package com.example.authstarter.features.auth.constants;

public final class SecurityConstants {

    private SecurityConstants() {}

    public static final String[] PUBLIC_URLS = {"/api/v1/auth/**"};
    public static final String[] SWAGGER_URLS = {"/swagger-ui/**", "/v3/api-docs/**"};
    public static final String[] WEBSOCKET_URLS = {"/ws/**"};
    public static final String[] WEBHOOK_URLS = {};
}