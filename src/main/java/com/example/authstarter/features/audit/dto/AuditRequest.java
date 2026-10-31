package com.example.authstarter.features.audit.dto;

import com.example.authstarter.features.audit.enums.AuditAction;
import com.example.authstarter.features.shared.dto.ClientInfo;
import com.example.authstarter.features.user.model.User;

import java.util.Map;
import java.util.UUID;

import static com.example.authstarter.features.shared.utils.ClientInfoUtils.getClientInfo;

/**
 * Snapshot of everything the audit log needs, taken at publish time. The listener runs on another
 * thread, so it must not touch a (possibly lazy or still-changing) User entity.
 */
public record AuditRequest(
        UUID userId,
        String email,
        String fullName,
        AuditAction auditAction,
        String description,
        String ipAddress,
        String userAgent,
        Map<String, Object> metaData
) {

    // Overload without metaData
    public static AuditRequest log(
            User user,
            AuditAction auditAction,
            String description
    ) {
        return log(user, auditAction, description, Map.of());
    }

    public static AuditRequest log(
            User user,
            AuditAction auditAction,
            String description,
            Map<String, Object> metaData
    ) {
        String name = user.displayName();
        ClientInfo clientInfo = getClientInfo();

        return new AuditRequest(
                user.getId(),
                user.getEmail(),
                name != null ? name : "Unknown",
                auditAction,
                description,
                clientInfo.ipAddress(),
                clientInfo.userAgent(),
                metaData != null ? metaData : Map.of()
        );
    }
}