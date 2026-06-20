package com.example.authstarter.features.auth.constants;

import java.time.Duration;

public final class RateLimitConstants {

    private RateLimitConstants () {}

    public static final String[] RATE_LIMITED_ENDPOINTS = {
            "/api/v1/auth/login",
            "/api/v1/auth/register",
            "/api/v1/auth/forgot-password",
            "/api/v1/auth/reset-password"
    };

    public static final int MAX_ATTEMPTS = 5;

    public static final Duration WINDOW = Duration.ofMinutes(1);
}
