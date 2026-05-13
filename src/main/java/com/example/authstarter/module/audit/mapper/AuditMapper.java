package com.example.authstarter.module.audit.mapper;

import com.example.authstarter.module.audit.dto.AuditResponse;
import com.example.authstarter.module.audit.model.AuditLog;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AuditMapper {

    AuditResponse toDto(AuditLog auditLog);
}
