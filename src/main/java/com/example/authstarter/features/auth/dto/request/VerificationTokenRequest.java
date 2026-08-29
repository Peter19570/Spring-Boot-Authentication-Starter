package com.example.authstarter.features.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

public record VerificationTokenRequest(
        @NotBlank(message = "Token is required")
        String token
) {
}
