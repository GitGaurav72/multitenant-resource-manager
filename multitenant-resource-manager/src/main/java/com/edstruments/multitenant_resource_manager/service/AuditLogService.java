package com.edstruments.multitenant_resource_manager.service;

import com.edstruments.multitenant_resource_manager.entity.AuditLog;
import com.edstruments.multitenant_resource_manager.entity.User;
import com.edstruments.multitenant_resource_manager.repository.AuditLogRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AuditLogService {

	@Autowired
    private AuditLogRepository auditLogRepository;

    @Transactional
    public void logUserAction(User user, String action, String details) {
        AuditLog auditLog = new AuditLog();
        auditLog.setUser(user);
        auditLog.setAction(action);
        auditLog.setTimestamp(LocalDateTime.now());
        auditLogRepository.save(auditLog);
    }

    @Transactional
    public void logResourceAction(User user, String action) {
        AuditLog auditLog = new AuditLog();
        auditLog.setUser(user);
        auditLog.setAction(action);
        auditLog.setTimestamp(LocalDateTime.now());
        auditLogRepository.save(auditLog);
    }

    public Page<AuditLog> getAuditLogs(Pageable pageable) {
        // This would typically filter by current tenant
        return auditLogRepository.findAllByOrderByTimestampDesc(pageable);
    }

    public Page<AuditLog> searchAuditLogs(String action, Long userId, Pageable pageable) {
        if (action != null && userId != null) {
            return auditLogRepository.findByActionAndUserIdOrderByTimestampDesc(action, userId, pageable);
        } else if (action != null) {
            return auditLogRepository.findByActionOrderByTimestampDesc(action, pageable);
        } else if (userId != null) {
            return auditLogRepository.findByUserIdOrderByTimestampDesc(userId, pageable);
        } else {
            return auditLogRepository.findAllByOrderByTimestampDesc(pageable);
        }
    }

    public Page<AuditLog> getAuditLogsByTenant(Long tenantId, Pageable pageable) {
        return auditLogRepository.findByUserTenantIdOrderByTimestampDesc(tenantId, pageable);
    }
}