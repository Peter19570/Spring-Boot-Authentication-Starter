package com.example.authstarter.features.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(
        @NotBlank(message = "Reset password token cannot be empty")
        String resetToken,

        @NotBlank(message = "New password is required")
        @Size(min = 6, max = 128, message = "Password must be between 8 and 128 characters")
        String newPassword
) {}
