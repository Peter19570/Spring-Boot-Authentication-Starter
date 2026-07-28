package com.example.authstarter.features.auth.service;

import com.example.authstarter.features.audit.dto.AuditRequest;
import com.example.authstarter.features.audit.enums.AuditAction;
import com.example.authstarter.features.auth.config.jwt.JwtService;
import com.example.authstarter.features.auth.dto.request.*;
import com.example.authstarter.features.auth.dto.response.AuthResponse;
import com.example.authstarter.features.auth.dto.response.NameParts;
import com.example.authstarter.features.auth.dto.response.PasskeyOptionsResponse;
import com.example.authstarter.features.auth.dto.response.TokenResponse;
import com.example.authstarter.features.auth.exceptions.AlreadyExistException;
import com.example.authstarter.features.auth.exceptions.AuthenticationException;
import com.example.authstarter.features.auth.exceptions.NotFoundException;
import com.example.authstarter.features.auth.exceptions.ValidationException;
import com.example.authstarter.features.auth.mapper.AuthMapper;
import com.example.authstarter.features.auth.model.EmailVerificationToken;
import com.example.authstarter.features.auth.model.PasswordResetToken;
import com.example.authstarter.features.auth.model.RefreshToken;
import com.example.authstarter.features.auth.repo.EmailVerificationTokenRepo;
import com.example.authstarter.features.auth.repo.PasskeyRepo;
import com.example.authstarter.features.auth.repo.PasswordResetTokenRepo;
import com.example.authstarter.features.auth.repo.RefreshTokenRepo;
import com.example.authstarter.features.auth.service.helpers.AuthHelper;
import com.example.authstarter.features.auth.service.notification.EmailService;
import com.example.authstarter.features.shared.dto.CustomUserPrincipal;
import com.example.authstarter.features.user.model.User;
import com.example.authstarter.features.user.repo.UserRepo;
import com.example.authstarter.features.user.service.UserService;
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
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static com.example.authstarter.features.auth.service.helpers.AuthHelper.hashToken;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final UserRepo userRepo;
    private final JwtService jwtService;
    private final AuthMapper authMapper;
    private final AuthHelper authHelper;
    private final PasskeyRepo passkeyRepo;
    private final UserService userService;
    private final EmailService emailService;
    private final GoogleIdTokenVerifier verifier;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepo refreshTokenRepo;
    private final ApplicationEventPublisher eventPublisher;
    private final AuthenticationManager authenticationManager;
    private final PasswordResetTokenRepo passwordResetTokenRepo;
    private final EmailVerificationTokenRepo emailVerificationTokenRepo;
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
        NameParts names = authHelper.handleUsernameFromEmail(email);

        if (userRepo.existsByEmail(request.email())) {
            throw new AlreadyExistException("Email already registered");
        }

        User user = authMapper.toEntityFromAuth(request, names.firstName(), names.lastName());
        user.setPassword(passwordEncoder.encode(request.password()));
        User newUser = userRepo.save(user);

        // Listener will send email to user
        eventPublisher.publishEvent(newUser);
        return authHelper.createAuthResponse(jwtService, newUser, AuditAction.REGISTER);
    }

    @CachePut(cacheNames = "users", key = "#result.userInfo.id")
    @CacheEvict(cacheNames = "all-users", allEntries = true)
    public AuthResponse login(AuthRequest request) {
        User user = userRepo.findByEmail(request.email())
                .orElseThrow(() -> new NotFoundException("User not found"));

        authHelper.handleDeletedAccount(user);
        authHelper.handleLockedAccount(user);

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password()));

            authHelper.handleLockReset(user);
            return authHelper.createAuthResponse(jwtService, user, AuditAction.LOCAL_LOGIN);

        } catch (BadCredentialsException e) {
            authHelper.handleFailedLoginAttempt(user);
            throw new AuthenticationException("Bad Credentials");
        }
    }

    public TokenResponse refresh(RefreshTokenRequest request) {
        String token = request.refreshToken();
        String userId = jwtService.extractUserId(token);

        User user = userService.fetchUser(UUID.fromString(userId));

        RefreshToken storedToken = refreshTokenRepo.findByTokenHash(token)
                .filter(refreshToken ->
                        !refreshToken.isRevoked() && refreshToken.getExpiresAt().isAfter(Instant.now()))
                .orElseThrow(() -> new NotFoundException("Refresh token is invalid or expired"));

        if (!jwtService.extractTokenType(storedToken.getTokenHash()).equals("refresh")){
            throw new IllegalStateException("Invalid token type. Refresh token required.");
        }

        storedToken.setRevoked(true);
        refreshTokenRepo.save(storedToken);

        return authHelper.createTokenResponse(jwtService, user);
    }

    public void logout(RefreshTokenRequest request, UUID userId) {
        User user = userService.fetchUser(userId);

        boolean revoked = refreshTokenRepo.findByTokenHash(request.refreshToken())
                .map(token -> {
                    token.setRevoked(true);
                    refreshTokenRepo.save(token);
                    return true;
                })
                .orElse(false);

        String message = (revoked) ? "User logout success" : "Logout attempted but token not found";

        eventPublisher.publishEvent(new AuditRequest(user, AuditAction.LOGOUT,
                Map.of("message", message)
        ));
    }

    @CachePut(cacheNames = "users", key = "#result.userInfo.id")
    @CacheEvict(cacheNames = "all-users", allEntries = true)
    public AuthResponse googleLogin(GoogleRequest request)
            throws GeneralSecurityException, IOException {
        GoogleIdToken idToken = verifier.verify(request.idToken());

        if (idToken == null){throw new ValidationException("Google token is invalid");}

        GoogleIdToken.Payload payload = idToken.getPayload();

        User user = authHelper.syncGoogleWithLocal(payload);
        authHelper.handleLockedAccount(user);
        authHelper.handleDeletedAccount(user);
        authHelper.handleLockReset(user);

        return authHelper.createAuthResponse(jwtService, user, AuditAction.OAUTH_LOGIN);
    }

    /**
     * PASSKEY AUTHENTICATION METHODS HERE
     */

    public PasskeyOptionsResponse startPasskeyRegistration(
            HttpServletRequest request, HttpServletResponse response) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        assert authentication != null;
        var optionsRequest = new ImmutablePublicKeyCredentialCreationOptionsRequest(authentication);

        PublicKeyCredentialCreationOptions options =
                relyingPartyOperations.createPublicKeyCredentialCreationOptions(optionsRequest);

        creationOptionsRepository.save(request, response, options);
        return PasskeyOptionsResponse.toResponse(options);
    }

    public CredentialRecord finishPasskeyRegistration(
            HttpServletRequest servletRequest, HttpServletResponse servletResponse,
            PasskeyRegistrationRequest request, CustomUserPrincipal principal) {

        PublicKeyCredentialCreationOptions options = creationOptionsRepository.load(servletRequest);
        RelyingPartyPublicKey publicKey = new RelyingPartyPublicKey(request.credential(), request.label());

        assert options != null;
        var registrationRequest = new ImmutableRelyingPartyRegistrationRequest(options, publicKey);

        CredentialRecord record = relyingPartyOperations.registerCredential(registrationRequest);

        User existingUser = userService.fetchUser(principal.id());
        authHelper.handleAuthProviders(existingUser, "PASSKEY");

        eventPublisher.publishEvent(
                new AuditRequest(existingUser, AuditAction.PASSKEY_LINK,
                        Map.of("message", "Passkey linked successfully")));

        creationOptionsRepository.save(servletRequest, servletResponse, null);
        return record;
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

        assert options != null;
        var authenticationRequest = new RelyingPartyAuthenticationRequest(options, request.credential());

        PublicKeyCredentialUserEntity userEntity = relyingPartyOperations.authenticate(authenticationRequest);
        UUID userId = UUID.fromString(new String(userEntity.getId().getBytes(), StandardCharsets.UTF_8));

        User user = userService.fetchUser(userId);
        authHelper.handleLockedAccount(user);
        authHelper.handleDeletedAccount(user);
        authHelper.handleLockReset(user);

        requestOptionsRepository.save(servletRequest, servletResponse, null);

        return authHelper.createAuthResponse(jwtService, user, AuditAction.PASSKEY_LOGIN);
    }

    public void deleteSavedPasskey(CustomUserPrincipal principal, String credentialId){
        passkeyRepo.deleteByCredentialIdAndUserId(credentialId, principal.id());
    }

    /**
     * EMAIL RELATED METHODS HERE
     */

    public void verifyEmail(String rawToken) {
        String hashedToken = hashToken(rawToken);

        EmailVerificationToken token = emailVerificationTokenRepo.findByTokenHash(hashedToken)
                .filter(t -> !t.isUsed() && t.getExpiresAt().isAfter(Instant.now()))
                .orElseThrow(() -> new AuthenticationException("Invalid or expired verification token"));

        token.setUsed(true);
        User user = token.getUser();
        user.setEmailVerified(true);

        userRepo.save(user);
        emailVerificationTokenRepo.save(token);

        eventPublisher.publishEvent(new AuditRequest(user, AuditAction.EMAIL_VERIFIED,
                Map.of("message", "Email verified successfully")));
    }

    public void resendVerificationEmail(CustomUserPrincipal principal){
        User user = userService.fetchUser(principal.id());
        boolean isEmailVerified = user.isEmailVerified();

        if (isEmailVerified){
            throw new IllegalStateException("Email has been verified already");
        }

        eventPublisher.publishEvent(user);
    }

    public void requestEmailChange(UUID userId, EmailChangeRequest request) {
        User user = userService.fetchUser(userId);

        if (user.getPassword() == null) {
            throw new ValidationException("Cannot reset email with empty password");
        }

        if (userRepo.existsByEmail(request.newEmail())) {
            throw new IllegalStateException("Email is already in use.");
        }

        String token = UUID.randomUUID().toString();
        EmailVerificationToken emailVerificationToken = new EmailVerificationToken();
        emailVerificationToken.setTokenHash(token);
        emailVerificationToken.setNewEmail(request.newEmail()); // Store the pending email in the token record
        emailVerificationToken.setUser(user);
        emailVerificationToken.setExpiresAt(Instant.now().plus(Duration.ofHours(2)));
        emailVerificationTokenRepo.save(emailVerificationToken);

        emailService.sendEmailChangeConfirmation(request.newEmail(), token);

        eventPublisher.publishEvent(new AuditRequest(user, AuditAction.EMAIL_CHANGE_REQUEST,
                Map.of("message", "Email change request success")));
    }

    public void confirmEmailChange(String token) {
        EmailVerificationToken emailVerificationToken = emailVerificationTokenRepo.findByTokenHash(token)
                .orElseThrow(() -> new NotFoundException("Invalid or expired token"));

        if (emailVerificationToken.getExpiresAt().isBefore(Instant.now())) {
            emailVerificationTokenRepo.delete(emailVerificationToken);
            throw new AuthenticationException("Token has expired");
        }

        User user = emailVerificationToken.getUser();
        String oldEmail = user.getEmail();
        String newEmail = emailVerificationToken.getNewEmail();

        user.setEmail(newEmail);
        userRepo.save(user);

        emailVerificationTokenRepo.delete(emailVerificationToken);

        eventPublisher.publishEvent(new AuditRequest(user, AuditAction.EMAIL_CHANGE_CONFIRM,
                Map.of("message", "User successfully changed email from " + oldEmail + " to " + newEmail)));
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

        String rawToken = UUID.randomUUID().toString();
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setUser(user);
        resetToken.setTokenHash(hashToken(rawToken));
        resetToken.setExpiresAt(Instant.now().plus(Duration.ofMinutes(15)));
        passwordResetTokenRepo.save(resetToken);

        emailService.sendPasswordResetEmail(user, rawToken);
        eventPublisher.publishEvent(new AuditRequest(user, AuditAction.PASSWORD_REQUEST,
                Map.of("message", "Password change requested for user")));
    }

    public void resetPassword(String rawToken, String newPassword) {
        String hashedToken = hashToken(rawToken);

        PasswordResetToken token = passwordResetTokenRepo.findByTokenHash(hashedToken)
                .filter(t -> !t.isUsed() && t.getExpiresAt().isAfter(Instant.now()))
                .orElseThrow(() -> new NotFoundException("Invalid or expired reset token"));

        User user = token.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setLocked(false);
        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);

        refreshTokenRepo.revokeAllByUserId(user.getId());
        token.setUsed(true);

        userRepo.save(user);
        passwordResetTokenRepo.save(token);

        eventPublisher.publishEvent(new AuditRequest(user, AuditAction.PASSWORD_RESET,
                Map.of("message", "User reset password successfully")));
    }

}