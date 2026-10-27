package com.example.authstarter.features.auth.constants;

public final class SecurityConstants {

    private SecurityConstants() {}

    public static final String UNAUTHORIZED_MSG = "Unauthorized";

    public static final String[] PATTERN_URLS = {"/**"};

    // Only health is public (uptime checks). Everything else needs a token.
    public static final String[] ACTUATOR_URLS = {"/actuator/health", "/actuator/health/**"};

    public static final String[] PUBLIC_URLS = {
            "/api/v1/auth/register",
            "/api/v1/auth/login",
            "/api/v1/auth/google",
            "/api/v1/auth/refresh",
            "/api/v1/auth/verify-email",
            "/api/v1/auth/confirm-email",
            "/api/v1/auth/passkeys/challenge",
            "/api/v1/auth/passkeys/login",
            "/api/v1/auth/forgot-password",
            "/api/v1/auth/reset-password",
            "/api/v1/auth/logout"
    };

    public static final String[] SWAGGER_URLS = {"/swagger-ui/**", "/v3/api-docs/**"};
}