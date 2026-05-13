package com.example.authstarter.module.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(
        @NotNull(message = "Reset password token is required")
        @NotBlank(message = "Reset password token cannot be empty")
        String token,

        @NotBlank(message = "New password is required")
        @Size(min = 6, message = "New password should be greater than 6 chars")
        String newPassword
) {}
