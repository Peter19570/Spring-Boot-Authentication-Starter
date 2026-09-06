package com.example.authstarter.features.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RefreshTokenRequest(
        @NotBlank(message = "Refresh token field cannot be empty")
        String refreshToken
) {}
