package com.example.authstarter.module.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AccountDeletionRequest(
        @NotBlank(message = "Password is required")
        @Size(min = 6, message = "Password should be greater than 6 characters")
        String password,

        @NotBlank(message = "Verification code is required")
        @Size(min = 6, max = 6, message = "OTP must be exactly 6 characters")
        String otp
) {}
