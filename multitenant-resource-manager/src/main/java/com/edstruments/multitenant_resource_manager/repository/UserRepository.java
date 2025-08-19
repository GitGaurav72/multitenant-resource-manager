package com.edstruments.multitenant_resource_manager.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.edstruments.multitenant_resource_manager.entity.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Find user by username within a specific tenant
    Optional<User> findByUsernameAndTenantId(String username, Long tenantId);

    // Find all non-deleted users for a tenant
    List<User> findByTenantIdAndIsDeletedFalse(Long tenantId);

    // Count all non-deleted users for a tenant (for quota enforcement)
    long countByTenantId(Long tenantId);

    // Check if username exists within a tenant
    boolean existsByUsernameAndTenantId(String username, Long tenantId);

    // Soft delete by setting isDeleted flag to true
    @Query("UPDATE User u SET u.isDeleted = true WHERE u.id = :id AND u.tenant.id = :tenantId")
    void softDelete(@Param("id") Long id, @Param("tenantId") Long tenantId);

    // Find all users with a specific role within a tenant
    List<User> findByTenantIdAndRoleAndIsDeletedFalse(Long tenantId, User.Role role);

    // Find non-deleted user by ID within a tenant
    @Query("SELECT u FROM User u WHERE u.id = :id AND u.tenant.id = :tenantId AND u.isDeleted = false")
    Optional<User> findByIdAndTenantId(@Param("id") Long id, @Param("tenantId") Long tenantId);

    // Find all non-deleted users with pagination
    @Query("SELECT u FROM User u WHERE u.tenant.id = :tenantId AND u.isDeleted = false")
    List<User> findAllByTenantIdWithPagination(@Param("tenantId") Long tenantId, org.springframework.data.domain.Pageable pageable);
    
//    
//    @Query("SELECT u FROM User u WHERE u.tenant.id = :tenantId AND u.isDeleted = false")
//    List<User> findByTenantIdAndIsDeletedFalse(@Param("tenantId") Long tenantId);
//
//    @Query("SELECT u FROM User u WHERE u.id = :id AND u.tenant.id = :tenantId AND u.isDeleted = false")
//    Optional<User> findByIdAndTenantId(@Param("id") Long id, @Param("tenantId") Long tenantId);
    
    @Modifying
    @Query("UPDATE User u SET u.isDeleted = true WHERE u.tenant.id = :tenantId")
    void softDeleteAllByTenantId(@Param("tenantId") Long tenantId);
}