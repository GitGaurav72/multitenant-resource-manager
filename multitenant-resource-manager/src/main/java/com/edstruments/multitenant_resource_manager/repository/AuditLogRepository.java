package com.edstruments.multitenant_resource_manager.repository;

import com.edstruments.multitenant_resource_manager.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    Page<AuditLog> findAllByOrderByTimestampDesc(Pageable pageable);

    @Query("SELECT a FROM AuditLog a WHERE a.user.tenant.id = :tenantId ORDER BY a.timestamp DESC")
    Page<AuditLog> findByUserTenantIdOrderByTimestampDesc(@Param("tenantId") Long tenantId, Pageable pageable);

    Page<AuditLog> findByActionOrderByTimestampDesc(String action, Pageable pageable);

    Page<AuditLog> findByUserIdOrderByTimestampDesc(Long userId, Pageable pageable);

    Page<AuditLog> findByActionAndUserIdOrderByTimestampDesc(String action, Long userId, Pageable pageable);
}