package com.edstruments.multitenant_resource_manager.controller;

import com.edstruments.multitenant_resource_manager.entity.AuditLog;
import com.edstruments.multitenant_resource_manager.service.AuditLogService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/audit-logs")
public class AuditLogController {

	@Autowired
    private AuditLogService auditLogService;


    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<AuditLog>> getAuditLogs( @RequestHeader("X-TenantID") String tenantId,Pageable pageable) {
        Page<AuditLog> auditLogs = auditLogService.getAuditLogs( pageable);
        return ResponseEntity.ok(auditLogs);
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<AuditLog>> searchAuditLogs(
    		 @RequestHeader("X-TenantID") String tenantId,
             @RequestParam(required = false) String action,
             @RequestParam(required = false) Long userId,
            Pageable pageable) {
        Page<AuditLog> auditLogs = auditLogService.searchAuditLogs( action, userId, pageable);
        return ResponseEntity.ok(auditLogs);
    }
}