package com.mohamedMoslemani.kyc.repository;

import com.mohamedMoslemani.kyc.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
}
