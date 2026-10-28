package com.example.authstarter.features.audit.service;

import com.example.authstarter.features.audit.dto.AuditRequest;
import com.example.authstarter.features.audit.dto.AuditResponse;
import com.example.authstarter.features.audit.mapper.AuditMapper;
import com.example.authstarter.features.audit.model.AuditLog;
import com.example.authstarter.features.audit.repo.AuditRepo;
import com.example.authstarter.features.shared.dto.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static com.example.authstarter.features.auth.constants.AsyncConstants.AUDIT_EXECUTOR;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuditService {

    private final AuditRepo auditRepo;
    private final AuditMapper auditMapper;

    @Async(AUDIT_EXECUTOR)
    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void createAuditLog(AuditRequest request) {
        AuditLog audit = AuditLog.builder()
                .userId(request.userId())
                .email(request.email())
                .fullName(request.fullName())
                .description(request.description())
                .ipAddress(request.ipAddress())
                .userAgent(request.userAgent())
                .action(request.auditAction())
                .metadata(request.metaData())
                .build();

        auditRepo.save(audit);
    }

    public PageResponse<AuditResponse> getAllAudits(Pageable pageable){
        Page<AuditLog> responses = auditRepo.findAll(pageable);
        return PageResponse.from(responses.map(auditMapper::toDto));
    }

    public PageResponse<AuditResponse> getUserAudits(UUID userId, Pageable pageable){
        Page<AuditLog> responses = auditRepo.findAllByUserId(userId, pageable);
        return PageResponse.from(responses.map(auditMapper::toDto));
    }

}
