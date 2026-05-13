package com.example.authstarter.module.auth.dto.response;

public record TokenResponse(
        String access,
        String refresh,
        long expiresAt
){}
