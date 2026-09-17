package com.example.authstarter.features.auth.service.helpers;

import com.example.authstarter.features.audit.dto.AuditRequest;
import com.example.authstarter.features.audit.enums.AuditAction;
import com.example.authstarter.features.auth.config.jwt.JwtService;
import com.example.authstarter.features.auth.dto.response.AuthResponse;
import com.example.authstarter.features.auth.dto.internal.NameParts;
import com.example.authstarter.features.auth.dto.response.TokenResponse;
import com.example.authstarter.features.auth.exceptions.AlreadyExistException;
import com.example.authstarter.features.auth.exceptions.AuthenticationException;
import com.example.authstarter.features.auth.exceptions.NotFoundException;
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
import java.util.UUID;

import static com.example.authstarter.features.audit.enums.AuditAction.*;
import static com.example.authstarter.features.shared.constants.CacheConstants.USERS;
import static com.example.authstarter.features.shared.utils.ClientInfoUtils.getClientInfo;

@Component
@RequiredArgsConstructor
public class AuthHelper {

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

        return idToken.getPayload();
    }

    public User syncGoogleWithLocal(GoogleIdToken.Payload payload){
        boolean isNewUser = userRepo.findByEmail(payload.getEmail()).isEmpty();

        User existingUser =  userRepo.findByEmail(payload.getEmail())
                .orElseGet(() -> {
                    User user = authMapper.toEntityFromGooglePayload(payload);

                    eventPublisher.publishEvent(AuditRequest.log(
                            user, REGISTER,
                            "User created account with Google login", getClientInfo(), Map.of()));

                    return userRepo.save(user);
                });

        processLockedAccount(existingUser);
        validateAccountNotDeleted(existingUser);
        resetAccountLock(existingUser);

        if (!isNewUser && (existingUser.getFirstName().equals("not-set")
                || existingUser.getLastName().equals("not-set"))) {

            authMapper.updateEntityFromGooglePayload(payload, existingUser);

            eventPublisher.publishEvent(AuditRequest.log(
                    existingUser, SOCIAL_LINK,
                    "Google account linked successfully", getClientInfo(), Map.of()));
        }

        resolveAuthProviders(existingUser, "GOOGLE");
        userRepo.save(existingUser);

        return existingUser;
    }

    public NameParts extractUsernameFromEmail(String email){
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

    public AuthResponse createAuthResponse(User user, AuditAction auditAction){
        eventPublisher.publishEvent(AuditRequest.log(
                user, auditAction, "User logged in successfully", getClientInfo(), Map.of()));

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
        int newAttempts = user.getFailedLoginAttempts() + 1;
        user.setFailedLoginAttempts(newAttempts);

        if (newAttempts >= 5) {
            user.setLocked(true);
            user.setLockedUntil(Instant.now().plus(Duration.ofMinutes(15)));
        }

        userRepo.save(user);

        eventPublisher.publishEvent(AuditRequest.log(user, LOGIN_ATTEMPT,
                "User attempted login with incorrect password", getClientInfo(),
                Map.of("message", "Failed login attempts: " + newAttempts)));
    }

    public void resolveAuthProviders(User user, String targetProvider){
        String provider = user.getProvider();

        if (provider == null) {
            user.setProvider(targetProvider);
        } else if (!provider.contains(targetProvider)) {
            user.setProvider(provider + "," + targetProvider);
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
