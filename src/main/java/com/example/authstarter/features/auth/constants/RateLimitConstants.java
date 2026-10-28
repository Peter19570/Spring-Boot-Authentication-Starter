package com.example.authstarter.features.auth.constants;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Refill;

import java.time.Duration;
import java.util.Map;

public final class RateLimitConstants {

    private RateLimitConstants () {}

    public static final String CONTENT_TYPE = "application/json";

    /** Single source of truth: the keys are the rate-limited paths (exact match). */
    public static final Map<String, Bandwidth> ENDPOINT_LIMITS = Map.ofEntries(
            Map.entry("/api/v1/auth/login", limit(5, Duration.ofMinutes(1))),
            Map.entry("/api/v1/auth/google", limit(10, Duration.ofMinutes(1))),
            Map.entry("/api/v1/auth/refresh", limit(10, Duration.ofMinutes(1))),
            Map.entry("/api/v1/auth/register", limit(3, Duration.ofHours(1))),
            Map.entry("/api/v1/auth/forgot-password", limit(3, Duration.ofHours(1))),
            Map.entry("/api/v1/auth/reset-password", limit(5, Duration.ofHours(1))),
            Map.entry("/api/v1/auth/change-email", limit(3, Duration.ofHours(1))),
            Map.entry("/api/v1/users/me/deletion-request", limit(3, Duration.ofHours(1))),
            Map.entry("/api/v1/auth/resend-verification-email", limit(2, Duration.ofHours(1))),
            Map.entry("/api/v1/auth/passkeys/challenge", limit(10, Duration.ofMinutes(1))),
            Map.entry("/api/v1/auth/passkeys/login", limit(10, Duration.ofMinutes(1)))
    );

    private static Bandwidth limit(long capacity, Duration period) {
        return Bandwidth.classic(capacity, Refill.greedy(capacity, period));
    }
}