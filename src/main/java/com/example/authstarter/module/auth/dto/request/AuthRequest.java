package com.example.authstarter.module.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AuthRequest(
        @NotNull(message = "Email is required")
        @NotBlank(message = "Email cannot be empty")
        @Email(message = "Enter a valid email address")
        String email,

        @NotNull(message = "Password is required")
        @NotBlank(message = "Password cannot be empty")
        @Size(min = 6, message = "Password should be greater than 6 chars")
        String password
) {
}
