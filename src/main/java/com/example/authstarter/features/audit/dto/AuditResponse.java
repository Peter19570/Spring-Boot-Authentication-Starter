package com.example.authstarter.features.audit.dto;

import com.example.authstarter.features.audit.enums.AuditAction;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record AuditResponse(
        UUID id,
        UUID userId,
        String email,
        String fullName,
        AuditAction action,
        String description,
        Map<String, Object> metadata,
        Instant createdAt
) {
}
