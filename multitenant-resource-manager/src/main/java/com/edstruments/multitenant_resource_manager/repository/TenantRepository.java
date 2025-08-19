package com.edstruments.multitenant_resource_manager.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.edstruments.multitenant_resource_manager.entity.Tenant;

@Repository
public interface TenantRepository extends JpaRepository<Tenant, Long> {

}
