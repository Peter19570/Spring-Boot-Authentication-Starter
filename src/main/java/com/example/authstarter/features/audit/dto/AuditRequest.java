package com.example.authstarter.features.audit.dto;

import com.example.authstarter.features.audit.enums.AuditAction;
import com.example.authstarter.features.shared.dto.ClientInfo;
import com.example.authstarter.features.user.model.User;

import java.util.Map;

public record AuditRequest(
        User user,
        AuditAction auditAction,
        String description,
        String ipAddress,
        String userAgent,
        Map<String, Object> metaData
) {
    public static AuditRequest log(
            User user,
            AuditAction auditAction,
            String description,
            ClientInfo clientInfo,
            Map<String, Object> metaData
    ){
        return new AuditRequest(
                user,
                auditAction,
                description,
                clientInfo.ipAddress(),
                clientInfo.userAgent(),
                metaData
        );
    }
}
