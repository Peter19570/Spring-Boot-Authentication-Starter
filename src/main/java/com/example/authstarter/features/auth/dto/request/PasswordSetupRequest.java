package com.example.authstarter.features.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PasswordSetupRequest(

        @NotBlank(message = "Password cannot be empty")
        @Size(max = 128, message = "Password must not exceed 128 characters")
        String password
) {
}
