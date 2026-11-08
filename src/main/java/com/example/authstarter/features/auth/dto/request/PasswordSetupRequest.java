package com.example.authstarter.features.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record PasswordSetupRequest(
        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 72, message = "Password must be between 8 and 72 characters")
        String password,

        @NotBlank(message = "Verification code is required")
        @Pattern(regexp = "^[0-9\\s]{6,12}$", message = "Code must be 6 digits")
        String otp
) {}
