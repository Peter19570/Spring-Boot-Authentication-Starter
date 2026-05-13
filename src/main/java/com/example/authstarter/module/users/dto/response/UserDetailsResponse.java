package com.example.authstarter.module.users.dto.response;

import java.util.UUID;

public record UserDetailsResponse(
        UUID id,
        String email,
        String firstName,
        String lastName,
        String picture,
        boolean emailVerified,
        String provider
) {
}
