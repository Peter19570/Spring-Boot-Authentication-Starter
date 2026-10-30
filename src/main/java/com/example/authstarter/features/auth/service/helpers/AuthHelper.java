package com.example.authstarter.features.auth.service.helpers;

import com.example.authstarter.features.audit.dto.AuditRequest;
import com.example.authstarter.features.audit.enums.AuditAction;
import com.example.authstarter.features.auth.config.jwt.JwtService;
import com.example.authstarter.features.auth.dto.response.AuthResponse;
import com.example.authstarter.features.auth.dto.internal.NameParts;
import com.example.authstarter.features.auth.dto.response.TokenResponse;
import com.example.authstarter.features.shared.exceptions.custom.AlreadyExistException;
import com.example.authstarter.features.shared.exceptions.custom.AuthenticationException;
import com.example.authstarter.features.shared.exceptions.custom.NotFoundException;
import com.example.authstarter.features.auth.mapper.AuthMapper;
import com.example.authstarter.features.auth.model.RefreshToken;
import com.example.authstarter.features.auth.repo.RefreshTokenRepo;
import com.example.authstarter.features.shared.dto.UserPrincipal;
import com.example.authstarter.features.user.mapper.UserMapper;
import com.example.authstarter.features.user.model.User;
import com.example.authstarter.features.user.repo.UserRepo;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
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
import java.util.Optional;
import java.util.UUID;

import static com.example.authstarter.features.audit.enums.AuditAction.*;
import static com.example.authstarter.features.shared.constants.CacheConstants.USERS;
import static com.example.authstarter.features.shared.utils.ClientInfoUtils.getClientInfo;

@Component
@RequiredArgsConstructor
public class AuthHelper {

    private static final String NOT_SET = "not-set";
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final Duration LOCK_DURATION = Duration.ofMinutes(15);

    private final UserRepo userRepo;
    private final UserMapper userMapper;
    private final AuthMapper authMapper;
    private final JwtService jwtService;
    private final CacheManager cacheManager;
    private final GoogleIdTokenVerifier verifier;
    private final RefreshTokenRepo refreshTokenRepo;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = USERS, key = "#userId")
    public User getUser(UUID userId){
        return userRepo.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    public void cacheUser(User user) {
        Cache cache = cacheManager.getCache(USERS);
        if (cache != null) cache.put(user.getId(), user);
    }

    public void updateCache(User user){
        Cache cache = cacheManager.getCache(USERS);
        if (cache != null){
            cache.evict(user.getId());
        }
    }

    public User getUserFromDatabase(UUID userId){
        return userRepo.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    public GoogleIdToken.Payload verifyGoogleToken(String idTokenString) {
        GoogleIdToken idToken;

        try {
            idToken = verifier.verify(idTokenString);
        } catch (GeneralSecurityException | IOException e) {
            throw new AuthenticationException("Failed to verify Google token");
        }

        if (idToken == null) {
            throw new AuthenticationException("Google token is invalid");
        }

        GoogleIdToken.Payload payload = idToken.getPayload();

        if (!Boolean.TRUE.equals(payload.getEmailVerified())) {
            throw new AuthenticationException("Google email is not verified");
        }

        return payload;
    }

    @Transactional
    public User syncGoogleWithLocal(GoogleIdToken.Payload payload){
        Optional<User> found = userRepo.findByEmail(payload.getEmail());

        if (found.isEmpty()) {
            User newUser = authMapper.toEntityFromGooglePayload(payload);
            resolveAuthProviders(newUser, "GOOGLE");
            User saved = userRepo.save(newUser);

            eventPublisher.publishEvent(AuditRequest.log(
                    saved, REGISTER,
                    "User created account with Google login", getClientInfo(), Map.of()));

            return saved;
        }

        User user = found.get();

        validateAccountNotDeleted(user);
        processLockedAccount(user);
        resetAccountLock(user);

        if (NOT_SET.equals(user.getFirstName()) || NOT_SET.equals(user.getLastName())) {
            authMapper.updateEntityFromGooglePayload(payload, user);

            eventPublisher.publishEvent(AuditRequest.log(
                    user, SOCIAL_LINK,
                    "Google account linked successfully", getClientInfo(), Map.of()));
        }

        resolveAuthProviders(user, "GOOGLE");
        return userRepo.save(user);
    }

    public NameParts extractUsernameFromEmail(String email){
        int atIndex = email.indexOf("@");
        String firstName = NOT_SET;
        String lastName = NOT_SET;

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

    public AuthResponse createAuthResponse(User user, AuditAction auditAction){
        eventPublisher.publishEvent(AuditRequest.log(
                user, auditAction, "Authentication successful: " + auditAction.name(), getClientInfo(), Map.of()));

        return new AuthResponse(true, createTokenResponse(user), userMapper.toDto(user));
    }

    public TokenResponse createTokenResponse(User user){
        UserPrincipal principal = UserPrincipal.fromDatabase(user);

        String access = jwtService.generateAccessToken(principal);
        String refresh = jwtService.generateRefreshToken(principal);
        Duration refreshExpiration = jwtService.getRefreshTokenExpiration();
        long accessExpiration = jwtService.getAccessTokenExpiration().toSeconds();

        RefreshToken rt = new RefreshToken();
        rt.setUser(user);
        rt.setTokenHash(hashToken(refresh));
        rt.setExpiresAt(Instant.now().plus(refreshExpiration));
        refreshTokenRepo.save(rt);

        return new TokenResponse(access, refresh, accessExpiration);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processFailedLoginAttempt(User user){
        // Fresh row, locked for this transaction: parallel wrong guesses are counted one at a time
        User fresh = userRepo.findByIdForUpdate(user.getId())
                .orElseThrow(() -> new NotFoundException("User not found"));

        // The caller's own unlock is rolled back with its transaction, so redo it here
        if (fresh.isLocked() && fresh.getLockedUntil() != null
                && fresh.getLockedUntil().isBefore(Instant.now())) {
            resetAccountLock(fresh);
        }

        int newAttempts = fresh.getFailedLoginAttempts() + 1;
        fresh.setFailedLoginAttempts(newAttempts);

        if (newAttempts >= MAX_FAILED_ATTEMPTS) {
            fresh.setLocked(true);
            fresh.setLockedUntil(Instant.now().plus(LOCK_DURATION));
        }

        eventPublisher.publishEvent(AuditRequest.log(fresh, LOGIN_ATTEMPT,
                "User attempted login with incorrect password", getClientInfo(),
                Map.of("message", "Failed login attempts: " + newAttempts)));
    }

    public void resolveAuthProviders(User user, String targetProvider){
        if (user.getProvider() == null) {
            user.setProvider(targetProvider);
        } else if (user.hasProvider(targetProvider)) {
            user.setProvider(user.getProvider() + "," + targetProvider);
        }
    }

    public void processLockedAccount(User user){
        if (user.isLocked()) {
            if (user.getLockedUntil() != null &&
                    user.getLockedUntil().isBefore(Instant.now())) {

                resetAccountLock(user);

            } else {
                eventPublisher.publishEvent(AuditRequest.log(
                        user, LOGIN_FAILURE, "Login failed", getClientInfo(),
                            Map.of("message", "User account locked temporarily")));

                throw new AuthenticationException("Account is temporarily locked. Try again later.");
            }
        }
    }

    public void validateAccountNotDeleted(User user){
        if (user.getDeletedAt() != null){
            throw new AuthenticationException("This account has been deleted.");
        }
    }

    public void resetAccountLock(User user){
        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        user.setLocked(false);
    }

    public void validateEmailNotRegistered(String email){
        if (userRepo.existsByEmail(email)) {
            throw new AlreadyExistException("Email already registered");
        }
    }

    public static String hashToken(String rawToken){
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256")
                    .digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Hashing failed", e);
        }
    }
}
