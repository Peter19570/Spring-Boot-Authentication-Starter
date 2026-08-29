package com.example.authstarter.features.auth.constants;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Refill;

import java.time.Duration;
import java.util.Map;

public final class RateLimitConstants {

    private RateLimitConstants () {}

    public static final String[] RATE_LIMITED_ENDPOINTS = {
            "/api/v1/auth/login",
            "/api/v1/auth/refresh",
            "/api/v1/auth/register",
            "/api/v1/auth/forgot-password",
            "/api/v1/auth/reset-password",
            "/api/v1/auth/resend-verification-email",
            "/api/v1/auth/passkeys/challenge"
    };

    public static final Map<String, Bandwidth> ENDPOINT_LIMITS = Map.of(
            "/api/v1/auth/login", Bandwidth.classic(5, Refill.greedy(5, Duration.ofMinutes(1))),
            "/api/v1/auth/refresh", Bandwidth.classic(10, Refill.greedy(10, Duration.ofMinutes(1))),
            "/api/v1/auth/register", Bandwidth.classic(3, Refill.greedy(3, Duration.ofHours(1))),
            "/api/v1/auth/forgot-password", Bandwidth.classic(3, Refill.greedy(3, Duration.ofHours(1))),
            "/api/v1/auth/reset-password", Bandwidth.classic(5, Refill.greedy(5, Duration.ofHours(1))),
            "/api/v1/auth/resend-verification-email", Bandwidth.classic(2, Refill.greedy(2, Duration.ofHours(1))),
            "/api/v1/auth/passkeys/challenge", Bandwidth.classic(10, Refill.greedy(10, Duration.ofMinutes(1)))
    );
}
