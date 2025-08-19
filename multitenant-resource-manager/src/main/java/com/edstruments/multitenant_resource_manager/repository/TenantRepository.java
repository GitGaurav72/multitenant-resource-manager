package com.edstruments.multitenant_resource_manager.repository;

import com.edstruments.multitenant_resource_manager.entity.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TenantRepository extends JpaRepository<Tenant, Long> {

    Optional<Tenant> findByName(String name);

    Optional<Tenant> findBySchemaName(String schemaName);

    boolean existsByName(String name);

    boolean existsBySchemaName(String schemaName);

    @Query("SELECT COUNT(u) FROM User u WHERE u.tenant.id = :tenantId AND u.isDeleted = false")
    long countUsersByTenantId(@Param("tenantId") Long tenantId);

    @Query("SELECT COUNT(r) FROM Resource r WHERE r.tenant.id = :tenantId AND r.isDeleted = false")
    long countResourcesByTenantId(@Param("tenantId") Long tenantId);
}