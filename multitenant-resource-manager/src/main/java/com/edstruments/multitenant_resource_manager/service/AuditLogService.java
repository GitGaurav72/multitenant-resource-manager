//package com.edstruments.multitenant_resource_manager.service;
//
//import com.edstruments.multitenant_resource_manager.entity.AuditLog;
//import com.edstruments.multitenant_resource_manager.entity.User;
//import com.edstruments.multitenant_resource_manager.repository.AuditLogRepository;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.LocalDateTime;
//
//@Service
//public class AuditLogService {
//
//	@Autowired
//    private AuditLogRepository auditLogRepository;
//
//    @Transactional
//    public void logUserAction(User user, String action, String details) {
//        AuditLog auditLog = new AuditLog();
//        auditLog.setUser(user);
//        auditLog.setAction(action);
//        auditLog.setTimestamp(LocalDateTime.now());
//        auditLogRepository.save(auditLog);
//    }
//
//    @Transactional
//    public void logResourceAction(User user, String action) {
//        AuditLog auditLog = new AuditLog();
//        auditLog.setUser(user);
//        auditLog.setAction(action);
//        auditLog.setTimestamp(LocalDateTime.now());
//        auditLogRepository.save(auditLog);
//    }
//
//    public Page<AuditLog> getAuditLogs(Pageable pageable) {
//        // This would typically filter by current tenant
//        return auditLogRepository.findAllByOrderByTimestampDesc(pageable);
//    }
//
//    public Page<AuditLog> searchAuditLogs(String action, Long userId, Pageable pageable) {
//        if (action != null && userId != null) {
//            return auditLogRepository.findByActionAndUserIdOrderByTimestampDesc(action, userId, pageable);
//        } else if (action != null) {
//            return auditLogRepository.findByActionOrderByTimestampDesc(action, pageable);
//        } else if (userId != null) {
//            return auditLogRepository.findByUserIdOrderByTimestampDesc(userId, pageable);
//        } else {
//            return auditLogRepository.findAllByOrderByTimestampDesc(pageable);
//        }
//    }
//
//    public Page<AuditLog> getAuditLogsByTenant(Long tenantId, Pageable pageable) {
//        return auditLogRepository.findByUserTenantIdOrderByTimestampDesc(tenantId, pageable);
//    }
//}

package com.edstruments.multitenant_resource_manager.service;

import com.edstruments.multitenant_resource_manager.entity.AuditLog;
import com.edstruments.multitenant_resource_manager.entity.User;
import com.edstruments.multitenant_resource_manager.repository.AuditLogRepository;
import com.edstruments.multitenant_resource_manager.tenant.TenantContext;

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
        // Get tenant ID from user or throw exception if not available
        String tenantId = getRequiredTenantId(user);
        
        TenantContext.setCurrentTenant(tenantId);
        try {
            AuditLog auditLog = new AuditLog();
            auditLog.setUser(user);
            auditLog.setAction(action);
           
            auditLog.setTimestamp(LocalDateTime.now());
            auditLogRepository.save(auditLog);
        } finally {
            TenantContext.clear();
        }
    }

    @Transactional
    public void logResourceAction(User user, String action) {
        // Get tenant ID from user or throw exception if not available
        String tenantId = getRequiredTenantId(user);
        
        TenantContext.setCurrentTenant(tenantId);
        try {
            AuditLog auditLog = new AuditLog();
            auditLog.setUser(user);
            auditLog.setAction(action);
            auditLog.setTimestamp(LocalDateTime.now());
            auditLogRepository.save(auditLog);
        } finally {
            TenantContext.clear();
        }
    }

    @Transactional(readOnly = true)
    public Page<AuditLog> getAuditLogs(Pageable pageable) {
        // Get tenant ID from context or throw exception
        String tenantId = getRequiredTenantIdFromContext();
        
        TenantContext.setCurrentTenant(tenantId);
        try {
            return auditLogRepository.findAllByOrderByTimestampDesc(pageable);
        } finally {
            TenantContext.clear();
        }
    }

    @Transactional(readOnly = true)
    public Page<AuditLog> searchAuditLogs(String action, Long userId, Pageable pageable) {
        // Get tenant ID from context or throw exception
        String tenantId = getRequiredTenantIdFromContext();
        
        TenantContext.setCurrentTenant(tenantId);
        try {
            if (action != null && userId != null) {
                return auditLogRepository.findByActionAndUserIdOrderByTimestampDesc(action, userId, pageable);
            } else if (action != null) {
                return auditLogRepository.findByActionOrderByTimestampDesc(action, pageable);
            } else if (userId != null) {
                return auditLogRepository.findByUserIdOrderByTimestampDesc(userId, pageable);
            } else {
                return auditLogRepository.findAllByOrderByTimestampDesc(pageable);
            }
        } finally {
            TenantContext.clear();
        }
    }

    @Transactional(readOnly = true)
    public Page<AuditLog> getAuditLogsByTenant(Long tenantId, Pageable pageable) {
        // For this method, we need to determine which schema to use based on tenantId
        // This assumes you have a way to map tenant entity ID to schema name
        String schemaName = convertTenantIdToSchemaName(tenantId);
        
        TenantContext.setCurrentTenant(schemaName);
        try {
            return auditLogRepository.findByUserTenantIdOrderByTimestampDesc(tenantId, pageable);
        } finally {
            TenantContext.clear();
        }
    }

    // Helper methods for tenant handling
    private String getRequiredTenantId(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        
        if (user.getTenant() == null) {
            throw new IllegalStateException("User must be associated with a tenant");
        }
        
        // Assuming your Tenant entity has a method to get the schema name
        // Replace with your actual implementation
        String schemaName = user.getTenant().getSchemaName();
        if (schemaName == null || schemaName.trim().isEmpty()) {
            throw new IllegalStateException("Tenant schema name is not available for user: " + user.getId());
        }
        
        return schemaName;
    }

    private String getRequiredTenantIdFromContext() {
        String tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null || tenantId.trim().isEmpty()) {
            throw new IllegalStateException("No tenant identifier specified in current context. " +
                                           "Please ensure TenantAwareRequestFilter is properly configured " +
                                           "or pass tenant ID explicitly.");
        }
        return tenantId;
    }

    private String convertTenantIdToSchemaName(Long tenantId) {
        // Implement your logic to convert tenant entity ID to schema name
        // This could be a database lookup, a naming convention, etc.
        // For example: "tenant_" + tenantId
        return "tenant_" + tenantId; // Replace with your actual logic
    }

    // Additional methods that accept explicit tenant ID
    @Transactional
    public void logUserActionWithExplicitTenant(String tenantId, User user, String action, String details) {
        TenantContext.setCurrentTenant(tenantId);
        try {
            AuditLog auditLog = new AuditLog();
            auditLog.setUser(user);
            auditLog.setAction(action);
            auditLog.setTimestamp(LocalDateTime.now());
            auditLogRepository.save(auditLog);
        } finally {
            TenantContext.clear();
        }
    }

    @Transactional(readOnly = true)
    public Page<AuditLog> getAuditLogsWithExplicitTenant(String tenantId, Pageable pageable) {
        TenantContext.setCurrentTenant(tenantId);
        try {
            return auditLogRepository.findAllByOrderByTimestampDesc(pageable);
        } finally {
            TenantContext.clear();
        }
    }
}