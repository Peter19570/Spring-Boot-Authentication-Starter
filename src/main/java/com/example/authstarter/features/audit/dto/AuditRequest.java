package com.example.authstarter.features.audit.dto;

import com.example.authstarter.features.audit.enums.AuditAction;
import com.example.authstarter.features.user.model.User;

import java.util.Map;

public record AuditRequest(
        User user,
        AuditAction auditAction,
        Map<String, Object> metaData
) {
}
