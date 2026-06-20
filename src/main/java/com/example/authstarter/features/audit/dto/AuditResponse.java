package com.example.authstarter.features.audit.dto;

import com.example.authstarter.features.audit.enums.AuditAction;

import java.util.Map;
import java.util.UUID;

public record AuditResponse(
        UUID userId,
        String email,
        String fullName,
        AuditAction action,
        Map<String, Object> metadata
) {
}
