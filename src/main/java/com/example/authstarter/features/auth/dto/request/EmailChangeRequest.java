package com.example.authstarter.features.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Locale;

public record EmailChangeRequest(
        @NotBlank(message = "New email is required")
        @Email(message = "Invalid email address")
        @Size(max = 254, message = "Email must not exceed 254 characters")
        String newEmail,

        @NotBlank(message = "Current password is required")
        String currentPassword
) {
    public EmailChangeRequest {
        newEmail = newEmail == null ? null : newEmail.trim().toLowerCase(Locale.ROOT);
    }
}