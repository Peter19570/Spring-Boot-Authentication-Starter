package com.example.authstarter.features.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Locale;

public record AuthRequest(
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email address")
        @Size(max = 254, message = "Email must not exceed 254 characters")
        String email,

        @NotBlank(message = "Password cannot be empty")
        @Size(max = 128, message = "Password must not exceed 128 characters")
        String password
) {
    public AuthRequest {
        email = email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }
}
