package com.example.authstarter.features.audit.dto;

import com.example.authstarter.features.audit.enums.AuditAction;
import com.example.authstarter.features.user.model.User;

import java.util.Map;

public record AuditRequest(
        User user,
        AuditAction auditAction,
        String description,
        Map<String, Object> metaData
) {
    public static AuditRequest log(
            User user,
            AuditAction auditAction,
            String description,
            Map<String, Object> metaData){
        return new AuditRequest(user, auditAction, description, metaData);
    }
}
