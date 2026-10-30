package com.example.authstarter.features.auth.service;

import com.example.authstarter.features.audit.dto.AuditRequest;
import com.example.authstarter.features.auth.config.jwt.JwtService;
import com.example.authstarter.features.auth.dto.internal.JwtClaims;
import com.example.authstarter.features.auth.dto.internal.Verification;
import com.example.authstarter.features.auth.dto.request.*;
import com.example.authstarter.features.auth.dto.response.AuthResponse;
import com.example.authstarter.features.auth.dto.internal.NameParts;
import com.example.authstarter.features.auth.dto.response.PasskeyOptionsResponse;
import com.example.authstarter.features.auth.dto.response.PasskeyResponse;
import com.example.authstarter.features.auth.dto.response.TokenResponse;
import com.example.authstarter.features.shared.exceptions.custom.AlreadyExistException;
import com.example.authstarter.features.shared.exceptions.custom.AuthenticationException;
import com.example.authstarter.features.shared.exceptions.custom.NotFoundException;
import com.example.authstarter.features.shared.exceptions.custom.ValidationException;
import com.example.authstarter.features.auth.mapper.AuthMapper;
import com.example.authstarter.features.auth.mapper.PasskeyMapper;
import com.example.authstarter.features.auth.model.Passkey;
import com.example.authstarter.features.auth.model.RefreshToken;
import com.example.authstarter.features.auth.repo.PasskeyRepo;
import com.example.authstarter.features.auth.repo.RefreshTokenRepo;
import com.example.authstarter.features.auth.service.helpers.AuthHelper;
import com.example.authstarter.features.auth.service.memory.EVTService;
import com.example.authstarter.features.auth.service.memory.PRTService;
import com.example.authstarter.features.auth.service.memory.PasskeyOptionsService;
import com.example.authstarter.features.auth.service.notification.EmailService;
import com.example.authstarter.features.user.model.User;
import com.example.authstarter.features.user.repo.UserRepo;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.webauthn.api.*;
import org.springframework.security.web.webauthn.management.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.example.authstarter.features.audit.enums.AuditAction.*;
import static com.example.authstarter.features.auth.constants.JwtConstants.REFRESH_GRACE;
import static com.example.authstarter.features.auth.constants.JwtConstants.REFRESH_VALUE;
import static com.example.authstarter.features.auth.service.helpers.AuthHelper.hashToken;
import static com.example.authstarter.features.shared.utils.ClientInfoUtils.getClientInfo;

@Service
@Transactional
@RequiredArgsConstructor
public class AuthService {

    private final UserRepo userRepo;
    private final JwtService jwtService;
    private final AuthMapper authMapper;
    private final AuthHelper authHelper;
    private final EVTService evtService;
    private final PRTService prtService;
    private final PasskeyRepo passkeyRepo;
    private final EmailService emailService;
    private final PasskeyMapper passkeyMapper;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepo refreshTokenRepo;
    private final ApplicationEventPublisher eventPublisher;
    private final PasskeyOptionsService passkeyOptionsService;
    private final WebAuthnRelyingPartyOperations relyingPartyOperations;

    /**
     * MAJOR AUTHENTICATION METHODS HERE
     */

    public AuthResponse register(RegisterRequest request) {
        String email = request.email();

        NameParts names = authHelper.extractUsernameFromEmail(email);
        authHelper.validateEmailNotRegistered(email);

        User user = authMapper.toEntityFromAuthRequest(request, names);
        user.setPassword(passwordEncoder.encode(request.password()));
        User savedUser = userRepo.save(user);

        String rawToken = evtService.generateEVT(savedUser.getId().toString(), null);
        emailService.sendVerificationEmail(savedUser, rawToken);

        authHelper.cacheUser(savedUser);
        return authHelper.createAuthResponse(savedUser, REGISTER);
    }

    public AuthResponse login(AuthRequest request) {
        User user = userRepo.findByEmailAndDeletedAtIsNull(request.email())
                .orElseThrow(() -> new AuthenticationException("Bad Credentials"));

        authHelper.processLockedAccount(user);

        if (user.getPassword() == null
                || !passwordEncoder.matches(request.password(), user.getPassword())) {
            authHelper.processFailedLoginAttempt(user);
            throw new AuthenticationException("Bad Credentials");
        }

        authHelper.resetAccountLock(user);
        authHelper.cacheUser(user);

        return authHelper.createAuthResponse(user, LOCAL_LOGIN);
    }

    @Transactional(noRollbackFor = AuthenticationException.class) // the theft response below must be committed
    public TokenResponse refresh(RefreshTokenRequest request) {
        String token = request.refreshToken();
        JwtClaims jwtClaims = jwtService.extractToken(token);

        if (!jwtClaims.tokenType().equals(REFRESH_VALUE)) {
            throw new AuthenticationException("Invalid token type. Refresh token required.");
        }

        User user = authHelper.getUser(jwtClaims.userId());
        String hash = hashToken(token);
        Instant now = Instant.now();

        int revoked = refreshTokenRepo.revokeIfActive(hash, now);

        if (revoked == 0) {

            boolean justRotated = refreshTokenRepo
                    .existsByTokenHashAndRevokedTrueAndRevokedAtAfterAndExpiresAtAfter(
                            hash, now.minus(REFRESH_GRACE), now);

            if (!justRotated) {
                // An already-rotated token replayed after the grace window: assume it was stolen, end every session
                refreshTokenRepo.findByTokenHash(hash)
                        .filter(RefreshToken::isRevoked)
                        .ifPresent(t -> {
                            refreshTokenRepo.deleteAllByUserId(user.getId());
                            eventPublisher.publishEvent(AuditRequest.log(user, TOKEN_REUSE_DETECTED,
                                    "Refresh token reuse detected; all sessions revoked",
                                    getClientInfo(), Map.of()));
                        });

                throw new AuthenticationException("Refresh token is invalid or expired");
            }
        }

        return authHelper.createTokenResponse(user);
    }

    public void logout(RefreshTokenRequest request) {
        // Delete rather than mark revoked: a replayed logged-out token is then simply "not found"
        refreshTokenRepo.findByTokenHash(hashToken(request.refreshToken())).ifPresent(token -> {
            User user = authHelper.getUserFromDatabase(token.getUser().getId());
            refreshTokenRepo.delete(token);
            authHelper.updateCache(user);

            eventPublisher.publishEvent(AuditRequest.log(
                    user, LOGOUT, "User logout success", getClientInfo(), Map.of()));
        });
    }

    public AuthResponse googleLogin(GoogleRequest request) {
        GoogleIdToken.Payload payload = authHelper.verifyGoogleToken(request.idToken());

        User user = authHelper.syncGoogleWithLocal(payload);
        authHelper.cacheUser(user);

        return authHelper.createAuthResponse(user, OAUTH_LOGIN);
    }

    /**
     * PASSKEY AUTHENTICATION METHODS HERE
     */

    public PasskeyOptionsResponse startPasskeyRegistration(UUID userId) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null){
            var optionsRequest = new ImmutablePublicKeyCredentialCreationOptionsRequest(authentication);

            PublicKeyCredentialCreationOptions options =
                    relyingPartyOperations.createPublicKeyCredentialCreationOptions(optionsRequest);

            passkeyOptionsService.saveRegistration(userId, options);
            return PasskeyOptionsResponse.toResponse(options);
        }

        throw new AuthenticationException("Authentication required to register a passkey");

    }

    public PasskeyResponse finishPasskeyRegistration(PasskeyRegistrationRequest request, UUID userId) {

        PublicKeyCredentialCreationOptions options = passkeyOptionsService.takeRegistration(userId);
        RelyingPartyPublicKey publicKey = new RelyingPartyPublicKey(request.credential(), request.label());

        if (options != null){
            var registrationRequest = new ImmutableRelyingPartyRegistrationRequest(options, publicKey);

            CredentialRecord record = relyingPartyOperations.registerCredential(registrationRequest);

            User existingUser = authHelper.getUserFromDatabase(userId);
            authHelper.resolveAuthProviders(existingUser, "PASSKEY");
            authHelper.updateCache(existingUser);

            eventPublisher.publishEvent(
                    AuditRequest.log(existingUser, PASSKEY_LINK,
                            "Passkey linked successfully", getClientInfo(), Map.of()));

            Passkey saved = passkeyRepo.findByCredentialId(record.getCredentialId().toBase64UrlString())
                    .orElseThrow(() -> new NotFoundException("Passkey not found"));

            return passkeyMapper.toDto(saved);
        }

        throw new ValidationException("Registration session missing or expired");
    }

    public PublicKeyCredentialRequestOptions startPasskeyAuthentication() {

        var optionsRequest = new ImmutablePublicKeyCredentialRequestOptionsRequest(null);

        PublicKeyCredentialRequestOptions options =
                relyingPartyOperations.createCredentialRequestOptions(optionsRequest);

        passkeyOptionsService.saveAuthentication(options);
        return options;
    }

    public AuthResponse finishPasskeyAuthentication(PasskeyLoginRequest request) {

        PublicKeyCredentialRequestOptions options = passkeyOptionsService.takeAuthentication(request.credential());

        if (options != null){
            var authenticationRequest = new RelyingPartyAuthenticationRequest(options, request.credential());

            PublicKeyCredentialUserEntity userEntity = relyingPartyOperations.authenticate(authenticationRequest);
            UUID userId = UUID.fromString(new String(userEntity.getId().getBytes(), StandardCharsets.UTF_8));

            User user = authHelper.getUserFromDatabase(userId);
            authHelper.processLockedAccount(user);
            authHelper.resetAccountLock(user);
            authHelper.cacheUser(user);

            return authHelper.createAuthResponse(user, PASSKEY_LOGIN);
        }

        throw new ValidationException("Login session missing or expired");

    }

    public List<PasskeyResponse> findAllUserPasskeys(UUID userId) {
        List<Passkey> passkeys = passkeyRepo.findAllByUserIdOrderByCreatedAtDesc(userId);
        return passkeyMapper.toDto(passkeys);
    }

    public void deleteSavedPasskey(UUID userId, UUID passkeyId) {
        // One statement scoped to the owner: another user's passkey can't be touched or even detected
        if (passkeyRepo.deleteByIdAndUserId(passkeyId, userId) == 0) {
            throw new NotFoundException("Passkey not found");
        }

        eventPublisher.publishEvent(AuditRequest.log(
                authHelper.getUserFromDatabase(userId), PASSKEY_UNLINK,
                "Passkey removed", getClientInfo(), Map.of()));
    }

    /**
     * EMAIL RELATED METHODS HERE
     */

    public void resendVerificationEmail(UUID userId) {
        User user = authHelper.getUser(userId);

        if (user.isEmailVerified()){
            throw new AlreadyExistException("Email has been verified already");
        }

        String rawToken = evtService.generateEVT(user.getId().toString(), null);
        emailService.resendVerificationEmail(user, rawToken);
    }

    public void verifyEmail(String token) {
        Verification verification = evtService.validateEVT(token);

        if (verification.newEmail() != null) {
            throw new ValidationException("Invalid verification token");
        }

        User user = authHelper.getUserFromDatabase(UUID.fromString(verification.userId()));
        user.setEmailVerified(true);

        authHelper.updateCache(user);

        eventPublisher.publishEvent(AuditRequest.log(user, EMAIL_VERIFIED,
                "Email verified successfully", getClientInfo(), Map.of()));
    }

    public void requestEmailChange(UUID userId, EmailChangeRequest request) {
        User user = authHelper.getUserFromDatabase(userId);
        String newEmail = request.newEmail();

        if (user.getPassword() == null) {
            throw new ValidationException("Set a password before changing your email");
        }

        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new ValidationException("Current password is incorrect");
        }

        authHelper.validateEmailNotRegistered(newEmail);

        String rawToken = evtService.generateEVT(user.getId().toString(), newEmail);
        emailService.sendEmailChangeConfirmation(newEmail, rawToken);
    }

    public void confirmEmailChange(String token) {
        Verification verification = evtService.validateEVT(token);
        String newEmail = verification.newEmail();

        if (newEmail == null) {
            throw new ValidationException("Invalid email change token");
        }

        // Someone may have registered this address since the token was issued
        authHelper.validateEmailNotRegistered(newEmail);

        User user = authHelper.getUserFromDatabase(UUID.fromString(verification.userId()));
        String oldEmail = user.getEmail();

        user.setEmail(newEmail);
        user.setEmailVerified(true);
        authHelper.updateCache(user);

        eventPublisher.publishEvent(AuditRequest.log(user, EMAIL_CHANGED,
                "User has changed email", getClientInfo(), Map.of(
                        "old email", oldEmail, "new email", newEmail)));
    }

    /**
     * PASSWORD RELATED METHODS HERE
     */

    public void requestPasswordReset(ForgotPasswordRequest request) {
        // Always return silently so this endpoint can't be used to discover which emails exist.
        userRepo.findByEmailAndDeletedAtIsNull(request.email()).ifPresent(user -> {
            boolean hasNoPassword = user.getPassword() == null;
            boolean isNotGoogleUser = user.hasProvider("GOOGLE");

            if (hasNoPassword && isNotGoogleUser) {
                emailService.sendSocialLoginReminder(user, user.getProvider());
                return;
            }

            String rawToken = prtService.generatePRT(user.getId().toString());
            emailService.sendPasswordResetEmail(user, rawToken);
        });
    }

    public void resetPassword(ResetPasswordRequest request) {
        String userId = prtService.validatePRT(request.resetToken());

        User user = authHelper.getUserFromDatabase(UUID.fromString(userId));
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        refreshTokenRepo.deleteAllByUserId(user.getId());
        user.setLocked(false);
        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);

        eventPublisher.publishEvent(AuditRequest.log(user, PASSWORD_RESET,
                "User reset password successfully", getClientInfo(), Map.of()));
    }

}