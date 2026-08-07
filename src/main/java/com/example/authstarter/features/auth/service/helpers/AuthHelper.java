package com.example.authstarter.features.auth.service.helpers;

import com.example.authstarter.features.audit.dto.AuditRequest;
import com.example.authstarter.features.audit.enums.AuditAction;
import com.example.authstarter.features.auth.config.jwt.JwtService;
import com.example.authstarter.features.auth.dto.response.AuthResponse;
import com.example.authstarter.features.auth.dto.response.NameParts;
import com.example.authstarter.features.auth.dto.response.TokenResponse;
import com.example.authstarter.features.auth.exceptions.AlreadyExistException;
import com.example.authstarter.features.auth.exceptions.AuthenticationException;
import com.example.authstarter.features.auth.exceptions.NotFoundException;
import com.example.authstarter.features.auth.mapper.AuthMapper;
import com.example.authstarter.features.auth.model.RefreshToken;
import com.example.authstarter.features.auth.repo.RefreshTokenRepo;
import com.example.authstarter.features.shared.dto.CustomUserPrincipal;
import com.example.authstarter.features.user.mapper.UserMapper;
import com.example.authstarter.features.user.model.User;
import com.example.authstarter.features.user.repo.UserRepo;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AuthHelper {

    private final UserRepo userRepo;
    private final UserMapper userMapper;
    private final AuthMapper authMapper;
    private final RefreshTokenRepo refreshTokenRepo;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "all-users", key = "#userId")
    public User fetchUser(UUID userId){
        return userRepo.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    public User fetchUserFresh(UUID userId){
        return userRepo.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    public AuthResponse createAuthResponse(JwtService jwtService, User user, AuditAction auditAction){
        eventPublisher.publishEvent(AuditRequest.log(user, auditAction,
                Map.of("message", "User logged in successfully")));

        return new AuthResponse(
                true,
                createTokenResponse(jwtService, user),
                userMapper.toDto(user)
        );
    }

    public TokenResponse createTokenResponse(JwtService jwtService, User user){
        CustomUserPrincipal principal = new CustomUserPrincipal(user);

        String access = jwtService.generateAccessToken(principal);
        String refresh = jwtService.generateRefreshToken(principal);
        long accessExpiration = jwtService.getAccessExpirationInSeconds();

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setTokenHash(refresh);
        refreshToken.setExpiresAt(Instant.now().plusSeconds(60 * 60 * 24 * 7));
        refreshTokenRepo.save(refreshToken);

        return new TokenResponse(access, refresh, accessExpiration);
    }

    public static String hashToken(String rawToken) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256")
                    .digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Hashing failed", e);
        }
    }

    public User syncGoogleWithLocal(GoogleIdToken.Payload payload){
        User existingUser =  userRepo.findByEmail(payload.getEmail())
                .orElseGet(() -> {
                    User user = authMapper.toEntityFromGooglePayload(payload);

                    eventPublisher.publishEvent(AuditRequest.log(user, AuditAction.REGISTER,
                            Map.of("message", "User created account with Google login")));

                    return userRepo.save(user);
                });

        handleLockedAccount(existingUser);
        handleDeletedAccount(existingUser);
        handleLockReset(existingUser);

        if (existingUser.getFirstName().equals("not-set")
                || existingUser.getLastName().equals("not-set")){
            authMapper.updateEntityFromGooglePayload(payload, existingUser);

            eventPublisher.publishEvent(AuditRequest.log(existingUser, AuditAction.SOCIAL_LINK,
                    Map.of("message", "Google account linked successfully")));
        }

        handleAuthProviders(existingUser, "GOOGLE");
        userRepo.save(existingUser);

        return existingUser;
    }

    public void handleAuthProviders(User user, String targetProvider){
        String provider = user.getProvider();

        if (provider == null) {
            user.setProvider(targetProvider);
        } else if (!provider.contains(targetProvider)) {
            user.setProvider(provider + "," + targetProvider);
        }
    }

    public void handleLockedAccount(User user){
        if (user.isLocked()) {
            if (user.getLockedUntil() != null && user.getLockedUntil().isBefore(Instant.now())) {
                user.setLocked(false);
                user.setFailedLoginAttempts(0);
                user.setLockedUntil(null);
            } else {

                eventPublisher.publishEvent(new AuditRequest(user, AuditAction.LOGIN_FAILURE,
                        Map.of("message", "Login failed")));

                throw new AuthenticationException("Account is temporarily locked. Try again later.");
            }
        }
    }

    public void handleDeletedAccount(User user){
        if (user.getDeletedAt() != null){
            throw new AuthenticationException("This account has been deleted.");
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleFailedLoginAttempt(User user){
        int newAttempts = user.getFailedLoginAttempts() + 1;
        user.setFailedLoginAttempts(newAttempts);

        if (newAttempts >= 5) {
            user.setLocked(true);
            user.setLockedUntil(Instant.now().plus(Duration.ofMinutes(15)));
        }

        userRepo.save(user);

        eventPublisher.publishEvent(AuditRequest.log(user, AuditAction.LOGIN_ATTEMPT,
                Map.of("message", "Failed login attempts: " + newAttempts)));
    }

    public void handleLockReset(User user){
        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        user.setLocked(false);
    }

    public NameParts handleUsernameFromEmail(String email){
        int atIndex = email.indexOf("@");
        String firstName = "not-set";
        String lastName = "not-set";

        if (atIndex > 0){
            String tempName = email.substring(0, atIndex);

            int dotIndex = tempName.indexOf(".");
            if (dotIndex > 0 && dotIndex < tempName.length() - 1){
                firstName = tempName.substring(0, dotIndex);
                lastName = tempName.substring(dotIndex + 1, atIndex);

            } else {
                firstName = tempName;
            }
        }

        return NameParts.names(firstName, lastName);
    }

    public void handleUsedEmail(String email){
        if (userRepo.existsByEmail(email)) {
            throw new AlreadyExistException("Email already registered");
        }
    }
}
