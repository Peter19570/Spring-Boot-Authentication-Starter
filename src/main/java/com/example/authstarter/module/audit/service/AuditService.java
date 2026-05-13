package com.example.authstarter.module.audit.service;

import com.example.authstarter.module.audit.dto.AuditRequest;
import com.example.authstarter.module.audit.dto.AuditResponse;
import com.example.authstarter.module.audit.enums.AuditAction;
import com.example.authstarter.module.audit.mapper.AuditMapper;
import com.example.authstarter.module.audit.model.AuditLog;
import com.example.authstarter.module.audit.repo.AuditRepo;
import com.example.authstarter.module.users.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuditService {

    private final AuditRepo auditRepo;
    private final AuditMapper auditMapper;

    @Async
    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleAuditEvent(AuditRequest request) {

        AuditLog audit = AuditLog.builder()
                .userId(request.user().getId())
                .fullName(request.user().getFirstName() + " " + request.user().getLastName())
                .action(request.auditAction())
                .metadata(request.metaData())
                .build();

        auditRepo.save(audit);
    }

    // Simple GET method to retrieve all audits in the database, advance later with filters
    public Page<AuditResponse> getAllAudits(Pageable pageable){
        Page<AuditLog> responses = auditRepo.findAll(pageable);
        return responses.map(auditMapper::toDto);
    }
}
