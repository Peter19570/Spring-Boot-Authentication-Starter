package com.example.authstarter.features.auth.service.memory;

import com.example.authstarter.features.auth.exceptions.NotFoundException;
import com.github.benmanes.caffeine.cache.Cache;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.concurrent.*;

import static com.example.authstarter.features.auth.service.helpers.AuthHelper.hashToken;

@Component
@RequiredArgsConstructor
public class OTPService { // One-Time Password Code

    private final Cache<String, String> otpStore;

    public String generateOtp(String userId) {
        String otpCode = String.format("%06d", ThreadLocalRandom.current().nextInt(1000000));
        otpStore.put(userId, hashToken(otpCode));
        return otpCode;
    }

    public void validateOtp(String userId, String otpCode) {
        String savedHash = otpStore.getIfPresent(userId);

        if (savedHash == null || !savedHash.equals(hashToken(otpCode))) {
            throw new NotFoundException("Invalid or expired code");
        }

        otpStore.invalidate(userId);
    }
}
