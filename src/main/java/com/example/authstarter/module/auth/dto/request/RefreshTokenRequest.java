package com.example.authstarter.module.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RefreshTokenRequest(
        @NotNull(message = "Refresh token field is required")
        @NotBlank(message = "Refresh token field cannot be empty")
        String refreshToken
) {}
