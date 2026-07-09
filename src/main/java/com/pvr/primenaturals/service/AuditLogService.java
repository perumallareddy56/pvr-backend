package com.pvr.primenaturals.service;

import com.pvr.primenaturals.entity.AuditLog;
import com.pvr.primenaturals.repository.AuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class AuditLogService {

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Transactional
    public void log(String action, String operatorEmail, String details, String ipAddress) {
        AuditLog log = AuditLog.builder()
                .action(action)
                .operatorEmail(operatorEmail)
                .details(details)
                .ipAddress(ipAddress)
                .build();
        auditLogRepository.save(log);
    }

    public List<AuditLog> getAllLogs() {
        return auditLogRepository.findAll();
    }
}
