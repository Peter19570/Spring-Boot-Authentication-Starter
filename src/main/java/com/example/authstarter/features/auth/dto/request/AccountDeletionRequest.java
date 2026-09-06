package com.example.authstarter.features.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AccountDeletionRequest(
        @NotBlank(message = "Password is required")
        @Size(min = 6, max = 128, message = "Password must be between 8 and 128 characters")
        String password,

        @NotBlank(message = "Verification code is required")
        @Size(min = 6, max = 6, message = "OTP must be 6 characters")
        String otp
) {}
