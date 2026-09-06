package com.example.authstarter.features.audit.controller;

import com.example.authstarter.features.audit.dto.AuditResponse;
import com.example.authstarter.features.audit.service.AuditService;
import com.example.authstarter.features.shared.dto.ApiResponse;
import com.example.authstarter.features.shared.dto.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static com.example.authstarter.features.shared.constants.ControllerConstants.MAX_PAGE_SIZE;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/audits")
@Tag(name = "Audit-Logging", description = "Light auditing in place to keep track of activities happening")
public class AuditController {

    private final AuditService auditService;

    @GetMapping
    @Operation(summary = "Retrieve audit records.")
    public ResponseEntity<ApiResponse<PageResponse<AuditResponse>>> getAllAudits(
            @RequestParam(defaultValue = "0") @PositiveOrZero int page
    ) {
        Pageable pageable = PageRequest.of(page, MAX_PAGE_SIZE, Sort.by("createdAt").descending());
        PageResponse<AuditResponse> responses = auditService.getAllAudits(pageable);
        return ResponseEntity.ok(ApiResponse.success("All Audit Logs", responses));
    }
}
