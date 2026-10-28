package com.example.authstarter.features.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RefreshTokenRequest(
        
        @NotBlank(message = "Refresh token field cannot be empty")
        @Size(max = 2048, message = "Refresh token is too long")
        String refreshToken
) {}