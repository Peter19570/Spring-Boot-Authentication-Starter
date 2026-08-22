package com.example.authstarter.features.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EmailChangeRequest(
        @NotBlank(message = "New email is required")
        @Email(message = "Please provide a valid email address")
        String newEmail
) {}
