package com.example.authstarter.features.auth.service.notification;

import com.github.benmanes.caffeine.cache.Cache;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.concurrent.*;

@Service
@RequiredArgsConstructor
public class OtpService {

    private final Cache<String, String> otpStore;

    public String generateOtp(String email) {
        String otpCode = String.format("%06d", ThreadLocalRandom.current().nextInt(1000000));

        otpStore.put(email, otpCode);
        return otpCode;
    }

    public boolean validateOtp(String email, String otpCode) {
        String savedCode = otpStore.getIfPresent(email);

        if (savedCode != null){
            boolean isValid = savedCode.equals(otpCode);

            if (isValid) {otpStore.invalidate(email);}
            return isValid;
        }

        return false;
    }
}
