package com.example.authstarter.features.audit.listeners;

import com.example.authstarter.features.audit.dto.AuditRequest;
import com.example.authstarter.features.audit.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuditListener {

    private final AuditService auditService;

    @EventListener
    public void onCreateAuditLog(AuditRequest request){
        auditService.handleAuditEvent(request);
    }
}
