package com.example.authstarter.features.user.dto.response;

import java.util.UUID;

public record UserResponse(
        UUID id,
        String email,
        String firstName,
        String lastName,
        String picture,
        boolean emailVerified
) {}
