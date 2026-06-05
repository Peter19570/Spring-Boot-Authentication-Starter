package com.example.authstarter.features.shared.dto;

import java.util.UUID;

public record UserProfile(
        UUID id,
        String email,
        String password

        // add many as needed, roles, relations etc
) {
}
