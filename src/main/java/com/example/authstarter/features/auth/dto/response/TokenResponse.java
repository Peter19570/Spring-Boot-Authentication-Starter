package com.example.authstarter.features.auth.dto.response;

public record TokenResponse(
        String access,
        String refresh,
        long expiresAt
){}
