package com.fraudshield.service;

import com.fraudshield.entity.AuditLog;
import com.fraudshield.repository.AuditLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditLogService {

    private static final Logger logger = LoggerFactory.getLogger(AuditLogService.class);
    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional
    public void log(Long userId, String email, String action, String resource, String ipAddress, String details) {
        try {
            AuditLog auditLog = new AuditLog(userId, email, action, resource, ipAddress, details);
            auditLogRepository.save(auditLog);
            logger.info("AUDIT: action='{}' user='{}' resource='{}' details='{}'", action, email, resource, details);
        } catch (Exception e) {
            logger.error("Failed to write audit log entry: {}", e.getMessage());
        }
    }
}
