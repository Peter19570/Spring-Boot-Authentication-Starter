package com.example.authstarter.features.audit.service;

import com.example.authstarter.features.audit.dto.AuditRequest;
import com.example.authstarter.features.audit.dto.AuditResponse;
import com.example.authstarter.features.audit.mapper.AuditMapper;
import com.example.authstarter.features.audit.model.AuditLog;
import com.example.authstarter.features.audit.repo.AuditRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

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
                .email(request.user().getEmail())
                .fullName(request.user().getFirstName() + " " + request.user().getLastName())
                .action(request.auditAction())
                .metadata(request.metaData())
                .build();

        auditRepo.save(audit);
    }

    public Page<AuditResponse> getAllAudits(Pageable pageable){
        Page<AuditLog> responses = auditRepo.findAll(pageable);
        return responses.map(auditMapper::toDto);
    }
}
