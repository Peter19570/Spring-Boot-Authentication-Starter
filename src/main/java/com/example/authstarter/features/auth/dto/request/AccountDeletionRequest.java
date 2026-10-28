package com.example.authstarter.features.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AccountDeletionRequest(
        // Optional: only required for accounts that have a password (Google / passkey-only accounts have none)
        @Size(max = 128, message = "Password must not exceed 128 characters")
        String password,

        @NotBlank(message = "Verification code is required")
        @Pattern(regexp = "^[0-9\\s]{6,12}$", message = "OTP must be 6 digits")
        String otp
) {}
