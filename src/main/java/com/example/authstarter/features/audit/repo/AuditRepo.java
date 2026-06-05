package com.example.authstarter.features.audit.repo;

import com.example.authstarter.features.audit.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditRepo extends JpaRepository<AuditLog, Long> {
}
