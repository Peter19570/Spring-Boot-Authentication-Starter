package com.example.authstarter.module.auth.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record GoogleRequest(
        @NotNull(message = "Google ID token is required")
        @Size(min = 50, message = "Google ID token should be greater than 50 chars")
        String idToken
) {
}
