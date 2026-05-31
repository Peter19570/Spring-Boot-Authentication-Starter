package com.example.authstarter.module.audit.dto;

import com.example.authstarter.module.audit.enums.AuditAction;
import com.example.authstarter.module.user.model.User;

import java.util.Map;

public record AuditRequest(
        User user,
        AuditAction auditAction,
        Map<String, Object> metaData
) {
}
