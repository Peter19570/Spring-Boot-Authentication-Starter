package com.example.authstarter.module.audit.dto;

import com.example.authstarter.module.audit.enums.AuditAction;

import java.util.Map;
import java.util.UUID;

public record AuditResponse(
        UUID userId,
        String fullName,
        AuditAction action,
        Map<String, Object> metadata
) {
}
