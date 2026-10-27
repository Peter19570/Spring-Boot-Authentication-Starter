package com.example.authstarter.features.auth.service.helpers;

import com.example.authstarter.features.auth.repo.RefreshTokenRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Component
@EnableScheduling
@RequiredArgsConstructor
public class TokenCleanupJob {

    private final RefreshTokenRepo refreshTokenRepo;

    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void purgeExpiredRefreshTokens() {
        refreshTokenRepo.deleteExpired(Instant.now());
    }
}