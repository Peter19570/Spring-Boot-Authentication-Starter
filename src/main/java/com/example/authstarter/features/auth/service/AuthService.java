package com.example.authstarter.features.auth.service;

import com.example.authstarter.features.audit.dto.AuditRequest;
import com.example.authstarter.features.audit.enums.AuditAction;
import com.example.authstarter.features.auth.config.jwt.JwtService;
import com.example.authstarter.features.auth.dto.internal.Verification;
import com.example.authstarter.features.auth.dto.request.*;
import com.example.authstarter.features.auth.dto.response.AuthResponse;
import com.example.authstarter.features.auth.dto.internal.NameParts;
import com.example.authstarter.features.auth.dto.response.PasskeyOptionsResponse;
import com.example.authstarter.features.auth.dto.response.TokenResponse;
import com.example.authstarter.features.auth.exceptions.AuthenticationException;
import com.example.authstarter.features.auth.exceptions.NotFoundException;
import com.example.authstarter.features.auth.exceptions.ValidationException;
import com.example.authstarter.features.auth.mapper.AuthMapper;
import com.example.authstarter.features.auth.model.RefreshToken;
import com.example.authstarter.features.auth.repo.PasskeyRepo;
import com.example.authstarter.features.auth.repo.RefreshTokenRepo;
import com.example.authstarter.features.auth.service.helpers.AuthHelper;
import com.example.authstarter.features.auth.service.memory.EVTService;
import com.example.authstarter.features.auth.service.memory.PRTService;
import com.example.authstarter.features.auth.service.notification.EmailService;
import com.example.authstarter.features.user.model.User;
import com.example.authstarter.features.user.repo.UserRepo;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.webauthn.api.*;
import org.springframework.security.web.webauthn.authentication.PublicKeyCredentialRequestOptionsRepository;
import org.springframework.security.web.webauthn.management.*;
import org.springframework.security.web.webauthn.registration.PublicKeyCredentialCreationOptionsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static com.example.authstarter.features.auth.service.helpers.AuthHelper.hashToken;

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
    private final GoogleIdTokenVerifier verifier;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepo refreshTokenRepo;
    private final ApplicationEventPublisher eventPublisher;
    private final AuthenticationManager authenticationManager;
    private final WebAuthnRelyingPartyOperations relyingPartyOperations;
    private final PublicKeyCredentialRequestOptionsRepository requestOptionsRepository;
    private final PublicKeyCredentialCreationOptionsRepository creationOptionsRepository;


    /**
     * MAJOR AUTHENTICATION METHODS HERE
     */

    @CachePut(cacheNames = "users", key = "#result.userInfo.id")
    @CacheEvict(cacheNames = "all-users", allEntries = true)
    public AuthResponse register(AuthRequest request) {
        String email = request.email();

        NameParts names = authHelper.extractUsernameFromEmail(email);
        authHelper.validateEmailNotRegistered(email);

        User user = authMapper.toEntityFromAuthRequest(request, names.firstName(), names.lastName());
        user.setPassword(passwordEncoder.encode(request.password()));
        User savedUser = userRepo.save(user);

        String rawToken = evtService.generateEVT(savedUser.getId().toString(), null);
        emailService.sendVerificationEmail(savedUser, rawToken);

        return authHelper.createAuthResponse(jwtService, savedUser, AuditAction.REGISTER);
    }

    @CachePut(cacheNames = "users", key = "#result.userInfo.id")
    @CacheEvict(cacheNames = "all-users", allEntries = true)
    public AuthResponse login(AuthRequest request) {
        User user = userRepo.findByEmail(request.email())
                .orElseThrow(() -> new NotFoundException("User not found"));

        authHelper.validateAccountNotDeleted(user);
        authHelper.processLockedAccount(user);

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password()));

            authHelper.resetAccountLock(user);
            return authHelper.createAuthResponse(jwtService, user, AuditAction.LOCAL_LOGIN);

        } catch (BadCredentialsException e) {
            authHelper.processFailedLoginAttempt(user);
            throw new AuthenticationException("Bad Credentials");
        }
    }

    public TokenResponse refresh(RefreshTokenRequest request) {
        String token = request.refreshToken();
        String userId = jwtService.extractUserId(token);

        if (!jwtService.extractTokenType(token).equals("rt")){
            throw new IllegalStateException("Invalid token type. Refresh token required.");
        }

        User user = authHelper.fetchUser(UUID.fromString(userId));

        RefreshToken storedToken = refreshTokenRepo.findByTokenHash(hashToken(token))
                .filter(rt -> !rt.isRevoked() && rt.getExpiresAt().isAfter(Instant.now()))
                .orElseThrow(() -> new NotFoundException("Refresh token is invalid or expired"));

        storedToken.setRevoked(true);
        return authHelper.createTokenResponse(jwtService, user);
    }

    @CachePut(cacheNames = "users", key = "#userId")
    @CacheEvict(cacheNames = "all-users", allEntries = true)
    public void logout(RefreshTokenRequest request, UUID userId) {
        User user = authHelper.fetchUser(userId);

        boolean revoked = refreshTokenRepo.findByTokenHash(request.refreshToken())
                .map(token -> {
                    token.setRevoked(true);
                    refreshTokenRepo.save(token);
                    return true;
                })
                .orElse(false);

        String message = (revoked) ? "User logout success" : "User logged out without token revoke";

        eventPublisher.publishEvent(AuditRequest.log(user, AuditAction.LOGOUT, message, Map.of()));
    }

    @CachePut(cacheNames = "users", key = "#result.userInfo.id")
    @CacheEvict(cacheNames = "all-users", allEntries = true)
    public AuthResponse googleLogin(GoogleRequest request)
            throws GeneralSecurityException, IOException {
        GoogleIdToken idToken = verifier.verify(request.idToken());

        if (idToken == null){throw new ValidationException("Google token is invalid");}

        GoogleIdToken.Payload payload = idToken.getPayload();

        User user = authHelper.syncGoogleWithLocal(payload);
        return authHelper.createAuthResponse(jwtService, user, AuditAction.OAUTH_LOGIN);
    }

    /**
     * PASSKEY AUTHENTICATION METHODS HERE
     */

    public PasskeyOptionsResponse startPasskeyRegistration(
            HttpServletRequest request, HttpServletResponse response) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null){
            var optionsRequest = new ImmutablePublicKeyCredentialCreationOptionsRequest(authentication);

            PublicKeyCredentialCreationOptions options =
                    relyingPartyOperations.createPublicKeyCredentialCreationOptions(optionsRequest);

            creationOptionsRepository.save(request, response, options);
            return PasskeyOptionsResponse.toResponse(options);
        }

        throw new IllegalStateException("User not found in context holder");

    }

    public CredentialRecord finishPasskeyRegistration(
            HttpServletRequest servletRequest, HttpServletResponse servletResponse,
            PasskeyRegistrationRequest request, UUID userId) {

        PublicKeyCredentialCreationOptions options = creationOptionsRepository.load(servletRequest);
        RelyingPartyPublicKey publicKey = new RelyingPartyPublicKey(request.credential(), request.label());

        if (options != null){
            var registrationRequest = new ImmutableRelyingPartyRegistrationRequest(options, publicKey);

            CredentialRecord record = relyingPartyOperations.registerCredential(registrationRequest);

            User existingUser = authHelper.fetchUserFresh(userId);
            authHelper.resolveAuthProviders(existingUser, "PASSKEY");

            eventPublisher.publishEvent(
                    AuditRequest.log(existingUser, AuditAction.PASSKEY_LINK,
                            "Passkey linked successfully", Map.of()));

            creationOptionsRepository.save(servletRequest, servletResponse, null);
            return record;
        }

        throw new IllegalStateException("Registration session missing or expired");
    }

    public PublicKeyCredentialRequestOptions startPasskeyAuthentication(
            HttpServletRequest request, HttpServletResponse response) {

        var optionsRequest = new ImmutablePublicKeyCredentialRequestOptionsRequest(null);

        PublicKeyCredentialRequestOptions options =
                relyingPartyOperations.createCredentialRequestOptions(optionsRequest);

        requestOptionsRepository.save(request, response, options);
        return options;
    }

    @CachePut(cacheNames = "users", key = "#result.userInfo.id")
    @CacheEvict(cacheNames = "all-users", allEntries = true)
    public AuthResponse finishPasskeyAuthentication(
            HttpServletRequest servletRequest, HttpServletResponse servletResponse,
            PasskeyLoginRequest request) {

        PublicKeyCredentialRequestOptions options = requestOptionsRepository.load(servletRequest);

        if (options != null){
            var authenticationRequest = new RelyingPartyAuthenticationRequest(options, request.credential());

            PublicKeyCredentialUserEntity userEntity = relyingPartyOperations.authenticate(authenticationRequest);
            UUID userId = UUID.fromString(new String(userEntity.getId().getBytes(), StandardCharsets.UTF_8));

            User user = authHelper.fetchUser(userId);
            authHelper.processLockedAccount(user);
            authHelper.validateAccountNotDeleted(user);
            authHelper.resetAccountLock(user);

            requestOptionsRepository.save(servletRequest, servletResponse, null);
            return authHelper.createAuthResponse(jwtService, user, AuditAction.PASSKEY_LOGIN);
        }

        throw new IllegalStateException("Login session missing or expired");

    }

    public void deleteSavedPasskey(UUID userId, String credentialId){
        passkeyRepo.deleteByCredentialIdAndUserId(credentialId, userId);
    }

    /**
     * EMAIL RELATED METHODS HERE
     */

    public void verifyEmail(VerificationTokenRequest request) {
        Verification verification = evtService.validateEVT(request.token());

        User user = authHelper.fetchUser(UUID.fromString(verification.userId()));
        user.setEmailVerified(true);

        eventPublisher.publishEvent(AuditRequest.log(user, AuditAction.EMAIL_VERIFIED,
                "Email verified successfully", Map.of()));
    }

    public void resendVerificationEmail(UUID userId){
        User user = authHelper.fetchUser(userId);

        if (user.isEmailVerified()){
            throw new IllegalStateException("Email has been verified already");
        }

        String rawToken = evtService.generateEVT(user.getId().toString(), null);
        emailService.sendVerificationEmail(user, rawToken);
    }

    public void requestEmailChange(UUID userId, EmailChangeRequest request) {
        User user = authHelper.fetchUser(userId);
        String newEmail = request.newEmail();

        if (user.getPassword() == null) {
            throw new IllegalStateException("Cannot reset email with empty password");
        }

        authHelper.validateEmailNotRegistered(newEmail);

        String rawToken = evtService.generateEVT(user.getId().toString(), newEmail);
        emailService.sendEmailChangeConfirmation(newEmail, rawToken);
    }

    @CacheEvict(cacheNames = "all-users", allEntries = true)
    public void confirmEmailChange(VerificationTokenRequest request) {
        Verification verification = evtService.validateEVT(request.token());
        User user = authHelper.fetchUserFresh(UUID.fromString(verification.userId()));

        String oldEmail = user.getEmail();
        String newEmail = verification.newEmail();

        user.setEmail(newEmail);

        eventPublisher.publishEvent(AuditRequest.log(user, AuditAction.EMAIL_CHANGED,
                "User has changed email", Map.of(
                        "old email", oldEmail, "new email", newEmail)));
    }

    /**
     * PASSWORD RELATED METHODS HERE
     */

    public void requestPasswordReset(ForgotPasswordRequest request) {
        User user = userRepo.findByEmailAndDeletedAtIsNull(request.email())
                .orElseThrow(() -> new NotFoundException("User not found"));

        boolean hasNoPassword = user.getPassword() == null;
        boolean isNotGoogleUser = !"GOOGLE".equals(user.getProvider());

        if (hasNoPassword && isNotGoogleUser) {
            emailService.sendSocialLoginReminder(user, user.getProvider());
            throw new IllegalStateException("Cannot request password-reset for non-local account");
        }

        String rawToken = prtService.generatePRT(user.getId().toString());
        emailService.sendPasswordResetEmail(user, rawToken);
    }

    public void resetPassword(ResetPasswordRequest request) {
        String userId = prtService.validatePRT(request.resetToken());

        User user = authHelper.fetchUserFresh(UUID.fromString(userId));
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        user.setLocked(false);
        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);

        eventPublisher.publishEvent(AuditRequest.log(user, AuditAction.PASSWORD_RESET,
                "User reset password successfully", Map.of()));
    }

}