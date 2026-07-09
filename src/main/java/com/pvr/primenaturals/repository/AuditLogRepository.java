package com.pvr.primenaturals.repository;

import com.pvr.primenaturals.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByOperatorEmailOrderByTimestampDesc(String operatorEmail);
    List<AuditLog> findByActionOrderByTimestampDesc(String action);
}
