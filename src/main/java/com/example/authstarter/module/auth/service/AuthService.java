package com.example.authstarter.module.auth.service;

import com.example.authstarter.module.audit.dto.AuditRequest;
import com.example.authstarter.module.audit.enums.AuditAction;
import com.example.authstarter.module.audit.service.AuditService;
import com.example.authstarter.module.auth.config.jwt.JwtService;
import com.example.authstarter.module.auth.dto.request.*;
import com.example.authstarter.module.auth.dto.response.AuthResponse;
import com.example.authstarter.module.auth.dto.response.TokenResponse;
import com.example.authstarter.module.auth.exceptions.AlreadyExistException;
import com.example.authstarter.module.auth.exceptions.AuthenticationException;
import com.example.authstarter.module.auth.exceptions.NotFoundException;
import com.example.authstarter.module.auth.exceptions.ValidationException;
import com.example.authstarter.module.auth.mapper.AuthMapper;
import com.example.authstarter.module.auth.model.EmailVerificationToken;
import com.example.authstarter.module.auth.model.PasswordResetToken;
import com.example.authstarter.module.auth.model.RefreshToken;
import com.example.authstarter.module.auth.repo.EmailVerificationTokenRepo;
import com.example.authstarter.module.auth.repo.PasswordResetTokenRepo;
import com.example.authstarter.module.auth.repo.RefreshTokenRepo;
import com.example.authstarter.module.auth.service.notification.EmailService;
import com.example.authstarter.module.shared.dto.CustomUserPrincipal;
import com.example.authstarter.module.users.mapper.UserMapper;
import com.example.authstarter.module.users.model.User;
import com.example.authstarter.module.users.repo.UserRepo;
import com.example.authstarter.module.users.service.UserService;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final UserRepo userRepo;
    private final RefreshTokenRepo refreshTokenRepo;
    private final PasswordResetTokenRepo passwordResetTokenRepo;
    private final EmailVerificationTokenRepo emailVerificationTokenRepo;

    private final UserService userService;
    private final EmailService emailService;
    private final JwtService jwtService;

    private final UserMapper userMapper;
    private final AuthMapper authMapper;

    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final GoogleIdTokenVerifier verifier;

    private final ApplicationEventPublisher eventPublisher;

//    =========================================================================================
//    MAJOR AUTHENTICATION METHODS HERE
//    =========================================================================================

    public AuthResponse register(AuthRequest request) {
        if (userRepo.existsByEmail(request.email())) {
            throw new AlreadyExistException("Email already registered");
        }

        User user = authMapper.toEntityFromAuth(request);
        user.setPassword(passwordEncoder.encode(request.password()));
        CustomUserPrincipal principal = new CustomUserPrincipal(userRepo.save(user));
        eventPublisher.publishEvent(principal.user());

        eventPublisher.publishEvent(new AuditRequest(principal.user(), AuditAction.REGISTER,
                Map.of("message", "New user created")));

        return createAuthResponse(jwtService, principal);
    }

    public AuthResponse login(AuthRequest request) {
        User user = userRepo.findByEmailAndDeletedAtIsNull(request.email())
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        if (user.isLocked()) {
            if (user.getLockedUntil() != null && user.getLockedUntil().isBefore(Instant.now())) {
                user.setLocked(false);
                user.setFailedLoginAttempts(0);
                user.setLockedUntil(null);
                userRepo.save(user);
            } else {
                throw new AuthenticationException("Account is temporarily locked. Try again later.");
            }

            eventPublisher.publishEvent(new AuditRequest(user, AuditAction.LOGIN_FAILURE,
                    Map.of("message", "Login failed")));
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password()));

            CustomUserPrincipal principal = (CustomUserPrincipal) authentication.getPrincipal();

            if (principal == null){
                throw new AuthenticationException("Unexpected principal type");
            }

            user.setFailedLoginAttempts(0);
            user.setLockedUntil(null);
            user.setLocked(false);
            userRepo.save(user);

            eventPublisher.publishEvent(new AuditRequest(user, AuditAction.LOGIN,
                    Map.of("message", "User logged in successfully")));

            return createAuthResponse(jwtService, principal);

        } catch (BadCredentialsException e) {
            int newAttempts = user.getFailedLoginAttempts() + 1;
            user.setFailedLoginAttempts(newAttempts);

            eventPublisher.publishEvent(new AuditRequest(user, AuditAction.LOGIN_ATTEMPT,
                    Map.of("message", "Failed login attempts: " + newAttempts)));

            if (newAttempts >= 5) {
                user.setLocked(true);
                user.setLockedUntil(Instant.now().plus(Duration.ofMinutes(15)));
            }

            userRepo.save(user);
            throw new AuthenticationException(e.getMessage());
        }
    }

    public TokenResponse refresh(RefreshTokenRequest request) {
        String token = request.refreshToken();
        String userId = jwtService.extractUserId(token);

        User user = userRepo.findByIdAndDeletedAtIsNull(UUID.fromString(userId))
                .orElseThrow(() -> new NotFoundException("User not found"));

        RefreshToken storedToken = refreshTokenRepo.findByTokenHash(token)
                .filter(refreshToken ->
                        !refreshToken.isRevoked() && refreshToken.getExpiresAt().isAfter(Instant.now()))
                .orElseThrow(() -> new NotFoundException("Refresh token is invalid or expired"));

        storedToken.setRevoked(true);
        refreshTokenRepo.save(storedToken);

        return createTokenResponse(jwtService, user);
    }

    public void logout(String refreshToken, User user) {
        eventPublisher.publishEvent(new AuditRequest(user, AuditAction.LOGOUT,
                Map.of("message", "User logout success")));

        refreshTokenRepo.findByTokenHash(refreshToken)
                .ifPresent(token -> {
                    token.setRevoked(true);
                    refreshTokenRepo.save(token);
                });
    }

    public AuthResponse googleLogin(GoogleRequest request) throws GeneralSecurityException, IOException {
        GoogleIdToken idToken = verifier.verify(request.idToken());

        if (idToken == null){throw new ValidationException("Google token is invalid");}

        GoogleIdToken.Payload payload = idToken.getPayload();
        CustomUserPrincipal principal = new CustomUserPrincipal(userService.syncUser(payload));

        return createAuthResponse(jwtService, principal);
    }

//    =========================================================================================
//    EMAIL RELATED METHODS HERE
//    =========================================================================================

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

    public void requestEmailChange(User user, EmailChangeRequest request) {
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

//    =========================================================================================
//    PASSWORD RELATED METHODS HERE
//    =========================================================================================

    public void requestPasswordReset(ForgotPasswordRequest request) {
        User user = userRepo.findByEmailAndDeletedAtIsNull(request.email())
                .orElseThrow(() -> new NotFoundException("User not found"));

        boolean hasNoPassword = user.getPassword() == null;
        boolean isNotGoogleUser = !"GOOGLE".equals(user.getProvider());

        if (hasNoPassword && isNotGoogleUser) {
            emailService.sendSocialLoginReminder(user.getEmail(), user.getProvider());
            return;
        }

        String rawToken = UUID.randomUUID().toString();
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setUser(user);
        resetToken.setTokenHash(hashToken(rawToken));
        resetToken.setExpiresAt(Instant.now().plus(Duration.ofMinutes(15)));
        passwordResetTokenRepo.save(resetToken);

        emailService.sendPasswordResetEmail(user.getEmail(), rawToken);
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
        user.setEmailVerified(true);

        refreshTokenRepo.revokeAllByUserId(user.getId());
        token.setUsed(true);

        userRepo.save(user);
        passwordResetTokenRepo.save(token);

        eventPublisher.publishEvent(new AuditRequest(user, AuditAction.PASSWORD_RESET,
                Map.of("message", "User reset password successfully")));
    }

//    =========================================================================================
//    PRIVATE HELPER METHODS HERE
//    =========================================================================================

    private AuthResponse createAuthResponse(JwtService jwtService, CustomUserPrincipal principal){
        return new AuthResponse(
                true,
                createTokenResponse(jwtService, principal.user()),
                userMapper.toDto(principal.user())
        );
    }

    private TokenResponse createTokenResponse(JwtService jwtService, User user){
        CustomUserPrincipal principal = new CustomUserPrincipal(user);

        String access = jwtService.generateAccessToken(principal);
        String refresh = jwtService.generateRefreshToken(principal);
        long accessExpiration = jwtService.getAccessExpirationInSeconds();

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(principal.user());
        refreshToken.setTokenHash(refresh);
        refreshToken.setExpiresAt(Instant.now().plusSeconds(60 * 60 * 24 * 7));
        refreshTokenRepo.save(refreshToken);

        return new TokenResponse(access, refresh, accessExpiration);
    }

    private String hashToken(String rawToken) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256")
                    .digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Hashing failed", e);
        }
    }
}
