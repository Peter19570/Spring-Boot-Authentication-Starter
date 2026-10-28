package com.example.authstarter.features.audit.controller;

import com.example.authstarter.features.audit.dto.AuditResponse;
import com.example.authstarter.features.audit.service.AuditService;
import com.example.authstarter.features.shared.dto.ApiResponse;
import com.example.authstarter.features.shared.dto.PageResponse;
import com.example.authstarter.features.shared.dto.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.example.authstarter.features.shared.constants.PageConstants.SORT;
import static org.springframework.data.domain.Sort.Direction.DESC;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/audits")
@Tag(name = "Audit-Logging", description = "Light auditing in place to keep track of activities happening")
public class AuditController {

    private final AuditService auditService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')") // every user's audit log: fail closed until a role system exists
    @Operation(summary = "Retrieve all audit records (admin only).")
    public ResponseEntity<ApiResponse<PageResponse<AuditResponse>>> getAllAudits(
            @ParameterObject @PageableDefault(sort = SORT, direction = DESC, size = 30) Pageable pageable
    ) {
        PageResponse<AuditResponse> responses = auditService.getAllAudits(pageable);
        return ResponseEntity.ok(ApiResponse.success("All Audit Logs", responses));
    }

    @GetMapping("/me")
    @Operation(summary = "Retrieve the authenticated user's own audit records.")
    public ResponseEntity<ApiResponse<PageResponse<AuditResponse>>> getMyAudits(
            @AuthenticationPrincipal UserPrincipal principal,
            @ParameterObject @PageableDefault(sort = SORT, direction = DESC, size = 30) Pageable pageable
    ) {
        PageResponse<AuditResponse> responses = auditService.getUserAudits(principal.id(), pageable);
        return ResponseEntity.ok(ApiResponse.success("My Audit Logs", responses));
    }
}
