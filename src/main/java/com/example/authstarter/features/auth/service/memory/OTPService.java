package com.example.authstarter.features.auth.service.memory;

import com.example.authstarter.features.shared.exceptions.custom.ValidationException;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import static com.example.authstarter.features.auth.service.helpers.AuthHelper.hashToken;

@Component
@RequiredArgsConstructor
public class OTPService { // One-Time Password Code

    private static final int MAX_ATTEMPTS = 5;

    private final SecureRandom random;
    private final Cache<String, String> otpStore;

    // Attempts per user. Counted atomically, so parallel guesses can't slip past the limit.
    private final Cache<String, AtomicInteger> attemptStore = Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofMinutes(5))
            .maximumSize(10_000)
            .build();

    public String generateOtp(String userId) {
        String otpCode = String.format("%06d", random.nextInt(1_000_000));
        otpStore.put(userId, hashToken(otpCode));
        attemptStore.invalidate(userId);

        return otpCode;
    }

    public void validateOtp(String userId, String otpCode) {
        int attempt = attemptStore.get(userId, k -> new AtomicInteger()).incrementAndGet();

        if (attempt > MAX_ATTEMPTS) {
            otpStore.invalidate(userId);
            throw new ValidationException("Too many attempts. Request a new code.");
        }

        String savedHash = otpStore.getIfPresent(userId);

        if (savedHash == null || !MessageDigest.isEqual(
                savedHash.getBytes(StandardCharsets.UTF_8),
                hashToken(otpCode).getBytes(StandardCharsets.UTF_8))) {
            throw new ValidationException("Invalid or expired code");
        }

        otpStore.invalidate(userId);
        attemptStore.invalidate(userId);
    }
}