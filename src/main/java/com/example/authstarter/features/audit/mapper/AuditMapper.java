package com.example.authstarter.features.audit.mapper;

import com.example.authstarter.features.audit.dto.AuditResponse;
import com.example.authstarter.features.audit.model.AuditLog;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AuditMapper {

    AuditResponse toDto(AuditLog auditLog);
}
