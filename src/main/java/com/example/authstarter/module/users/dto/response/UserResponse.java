package com.example.authstarter.module.users.dto.response;

import java.util.UUID;

public record UserResponse(
        UUID id,
        String email,
        String firstName,
        String lastName,
        String picture,
        boolean emailVerified
) {}
