package com.edstruments.multitenant_resource_manager.service;

import com.edstruments.multitenant_resource_manager.exception.ResourceNotFoundException;
import com.edstruments.multitenant_resource_manager.entity.Tenant;
import com.edstruments.multitenant_resource_manager.repository.TenantRepository;
import com.edstruments.multitenant_resource_manager.repository.UserRepository;
import com.edstruments.multitenant_resource_manager.repository.ResourceRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TenantService {

	@Autowired
    private TenantRepository tenantRepository;
	@Autowired
    private UserRepository userRepository;
	@Autowired
    private ResourceRepository resourceRepository;
	@Autowired
    private DatabaseSchemaService databaseSchemaService;


    @Transactional
    public Tenant createTenant(Tenant tenant) {
        // Validate unique constraints
        if (tenantRepository.existsByName(tenant.getName())) {
            throw new IllegalArgumentException("Tenant name already exists: " + tenant.getName());
        }

        if (tenantRepository.existsBySchemaName(tenant.getSchemaName())) {
            throw new IllegalArgumentException("Schema name already exists: " + tenant.getSchemaName());
        }

        // Create the database schema for the tenant
        databaseSchemaService.createSchema(tenant.getSchemaName(),"1234");

        // Save the tenant
        Tenant savedTenant = tenantRepository.save(tenant);

        return savedTenant;
    }

    @Transactional
    public void deleteTenant(Long id) {
        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant", "id", id));

        // Soft delete all users in this tenant
        userRepository.softDeleteAllByTenantId(id);

        // Soft delete all resources in this tenant
        resourceRepository.softDeleteAllByTenantId(id);

        // Drop the tenant schema (optional - you might want to keep data for compliance)
        // databaseSchemaService.dropSchema(tenant.getSchemaName());

        // Delete the tenant record
        tenantRepository.delete(tenant);
    }

    public List<Tenant> getAllTenants() {
        return tenantRepository.findAll();
    }

    public Tenant getTenantById(Long id) {
        return tenantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant", "id", id));
    }

    public Tenant getTenantBySchemaName(String schemaName) {
        return tenantRepository.findBySchemaName(schemaName)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant", "schemaName", schemaName));
    }

    public long getUserCount(Long tenantId) {
        return tenantRepository.countUsersByTenantId(tenantId);
    }

    public long getResourceCount(Long tenantId) {
        return tenantRepository.countResourcesByTenantId(tenantId);
    }

    @Transactional
    public Tenant updateTenant(Long id, Tenant tenantDetails) {
        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant", "id", id));

        // Check if new name is unique (if changed)
        if (tenantDetails.getName() != null && !tenant.getName().equals(tenantDetails.getName())) {
            if (tenantRepository.existsByName(tenantDetails.getName())) {
                throw new IllegalArgumentException("Tenant name already exists: " + tenantDetails.getName());
            }
            tenant.setName(tenantDetails.getName());
        }

        // Schema name should not be changed as it's tied to the database schema
        if (tenantDetails.getSchemaName() != null && !tenant.getSchemaName().equals(tenantDetails.getSchemaName())) {
            throw new IllegalArgumentException("Schema name cannot be changed after creation");
        }

        return tenantRepository.save(tenant);
    }
}