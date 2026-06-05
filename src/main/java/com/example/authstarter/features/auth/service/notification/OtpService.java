package com.example.authstarter.features.auth.service.notification;

import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.stereotype.Service;

import java.util.concurrent.*;

@Service
public class OtpService {

    private final ConcurrentHashMap<String, OtpData> otpStore =
            new ConcurrentHashMap<>();

    private final ScheduledExecutorService cleanupExecutor =
            Executors.newSingleThreadScheduledExecutor();

    @PostConstruct
    public void init() {
        // Clean up expired OTPs every minute
        cleanupExecutor.scheduleAtFixedRate(this::cleanupExpiredOtps, 1, 1, TimeUnit.MINUTES);
    }

    public String generateOtp(String identifier) {
        String otpCode = String.format("%06d", ThreadLocalRandom.current().nextInt(1000000));

        OtpData otpData = new OtpData(otpCode, System.currentTimeMillis() + 300000); // 5 min expiry
        otpStore.put(identifier, otpData);
        return otpCode;
    }

    public boolean validateOtp(String identifier, String otpCode) {
        OtpData otpData = otpStore.get(identifier);

        if (otpData == null) {
            return false;
        }

        if (System.currentTimeMillis() > otpData.expiryTime) {
            otpStore.remove(identifier);
            return false;
        }

        boolean isValid = otpData.otpCode.equals(otpCode);

        if (isValid) {
            otpStore.remove(identifier);
        }

        return isValid;
    }

    private void cleanupExpiredOtps() {
        long now = System.currentTimeMillis();
        otpStore.entrySet().removeIf(entry -> now > entry.getValue().expiryTime);
    }

    @Data
    @AllArgsConstructor
    private static class OtpData {
        private String otpCode;
        private long expiryTime;
    }
}
