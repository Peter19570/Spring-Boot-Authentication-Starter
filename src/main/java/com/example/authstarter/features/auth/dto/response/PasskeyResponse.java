package com.example.authstarter.features.auth.dto.response;

import java.time.Instant;
import java.util.UUID;

public record PasskeyResponse(
        UUID id,
        String label,
        Instant lastUsed,
        Instant createdAt
) {
}
