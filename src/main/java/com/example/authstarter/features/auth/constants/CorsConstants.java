package com.example.authstarter.features.auth.constants;

import java.util.List;

public final class CorsConstants {

    private CorsConstants() {}

    // yeah, I think this should go into the .env
    public static final List<String> ALLOWED_ORIGINS = List.of("http://localhost:3000");

    public static final List<String> ALLOWED_METHODS = List.of(
            "GET",
            "POST",
            "PUT",
            "DELETE",
            "OPTIONS",
            "PATCH"
    );

    public static final List<String> ALLOWED_HEADERS = List.of(
            "Authorization",
            "Content-Type",
            "Accept",
            "X-Requested-With",
            "ngrok-skip-browser-warning"
    );

    public static final List<String> ALLOWED_EXPOSED_HEADERS = List.of("Authorization");

    public static final String PATTERN = "/**";
}
