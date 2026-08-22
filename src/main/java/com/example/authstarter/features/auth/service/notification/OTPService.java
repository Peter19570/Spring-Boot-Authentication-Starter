package com.example.authstarter.features.auth.service.notification;

import com.example.authstarter.features.auth.service.helpers.AuthHelper;
import com.github.benmanes.caffeine.cache.Cache;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.concurrent.*;

@Component
@RequiredArgsConstructor
public class OTPService {

    private final Cache<String, String> otpStore;

    public String generateOtp(String email) {
        String otpCode = String.format("%06d", ThreadLocalRandom.current().nextInt(1000000));

        otpStore.put(email, AuthHelper.hashToken(otpCode));
        return otpCode;
    }

    public boolean validateOtp(String email, String otpCode) {
        String savedCode = otpStore.getIfPresent(email);

        if (savedCode != null){
            boolean isValid = savedCode.equals(AuthHelper.hashToken(otpCode));

            if (isValid) {otpStore.invalidate(email);}
            return isValid;
        }

        return false;
    }
}
