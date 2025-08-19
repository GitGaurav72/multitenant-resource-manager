package com.edstruments.multitenant_resource_manager.repository;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.edstruments.multitenant_resource_manager.entity.Resource;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResourceRepository extends JpaRepository<Resource, Long> {

    // Find all non-deleted resources for a tenant with pagination
    Page<Resource> findByTenantIdAndIsDeletedFalse(Long tenantId, Pageable pageable);

    // Find all non-deleted resources for a specific owner within a tenant
    Page<Resource> findByTenantIdAndOwnerIdAndIsDeletedFalse(Long tenantId, Long ownerId, Pageable pageable);

    // Count all non-deleted resources for a user (for quota enforcement)
    long countByOwnerId(Long ownerId);

    // Count all non-deleted resources for a tenant (for quota enforcement)
    long countByTenantId(Long tenantId);

    // Soft delete by setting isDeleted flag to true
    @Query("UPDATE Resource r SET r.isDeleted = true WHERE r.id = :id AND r.tenant.id = :tenantId")
    void softDelete(@Param("id") Long id, @Param("tenantId") Long tenantId);

    // Search resources with filters (name contains, description contains)
    @Query("SELECT r FROM Resource r WHERE " +
           "r.tenant.id = :tenantId AND " +
           "r.isDeleted = false AND " +
           "(:name IS NULL OR LOWER(r.name) LIKE LOWER(concat('%', :name, '%'))) AND " +
           "(:description IS NULL OR LOWER(r.description) LIKE LOWER(concat('%', :description, '%')))")
    Page<Resource> searchResources(
            @Param("tenantId") Long tenantId,
            @Param("name") String name,
            @Param("description") String description,
            Pageable pageable);

    // Find resource by ID within a specific tenant
    @Query("SELECT r FROM Resource r WHERE r.id = :id AND r.tenant.id = :tenantId AND r.isDeleted = false")
    Optional<Resource> findByIdAndTenantId(@Param("id") Long id, @Param("tenantId") Long tenantId);
    
    @Modifying
    @Query("UPDATE Resource r SET r.isDeleted = true WHERE r.tenant.id = :tenantId")
    void softDeleteAllByTenantId(@Param("tenantId") Long tenantId);
}