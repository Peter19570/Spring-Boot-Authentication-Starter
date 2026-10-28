package com.example.authstarter.features.audit.repo;

import com.example.authstarter.features.audit.model.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AuditRepo extends JpaRepository<AuditLog, UUID> {

    Page<AuditLog> findAllByUserId(UUID userId, Pageable pageable);
}