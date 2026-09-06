package com.example.authstarter.features.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record GoogleRequest(
        @NotBlank(message = "Google ID token is required")
        @Size(min = 50, max = 4096, message = "Google ID token must be between 50 and 4096 characters")
        String idToken
) {
}
