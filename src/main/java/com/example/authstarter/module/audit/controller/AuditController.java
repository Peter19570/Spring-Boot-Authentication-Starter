package com.example.authstarter.module.audit.controller;

import com.example.authstarter.module.audit.dto.AuditResponse;
import com.example.authstarter.module.audit.service.AuditService;
import com.example.authstarter.module.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/audits")
@RequiredArgsConstructor
@Tag(name = "Audit-Logging")
public class AuditController {

    private final AuditService auditService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<AuditResponse>>> getAllAudits(
            @RequestParam(defaultValue = "0") int page){
        Pageable pageable = PageRequest.of(page, 50, Sort.by("createdAt").descending());
        Page<AuditResponse> responses = auditService.getAllAudits(pageable);
        return ResponseEntity.ok(new ApiResponse<>("All Audit Logs", responses));
    }
}
