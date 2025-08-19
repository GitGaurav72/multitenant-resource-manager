package com.edstruments.multitenant_resource_manager.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.edstruments.multitenant_resource_manager.entity.Resource;
import com.edstruments.multitenant_resource_manager.entity.User;
import com.edstruments.multitenant_resource_manager.exception.QuotaExceededException;
import com.edstruments.multitenant_resource_manager.exception.ResourceNotFoundException;
import com.edstruments.multitenant_resource_manager.exception.UnauthorizedAccessException;
import com.edstruments.multitenant_resource_manager.repository.ResourceRepository;
import com.edstruments.multitenant_resource_manager.repository.UserRepository;

import jakarta.transaction.Transactional;

@Service
public class ResourceService {
	
	@Autowired
    private ResourceRepository resourceRepository;
	
	@Autowired
	private UserRepository userRepository;

  

    public Page<Resource> getAllResources(Pageable pageable) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return resourceRepository.findByTenantIdAndIsDeletedFalse(currentUser.getTenant().getId(), pageable);
    }

    public Resource createResource(Resource resource) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        
        // Check resource limit per user
        long userResourceCount = resourceRepository.countByOwnerId(currentUser.getId());
        if (userResourceCount >= 10) {
            throw new RuntimeException("User cannot have more than 10 resources");
        }
        
        // Check tenant resource limit
        long tenantResourceCount = resourceRepository.countByTenantId(currentUser.getTenant().getId());
        if (tenantResourceCount >= 500) {
            throw new RuntimeException("Tenant cannot have more than 500 resources");
        }

        resource.setOwner(currentUser);
        resource.setTenant(currentUser.getTenant());
        return resourceRepository.save(resource);
    }

    @Transactional
    public Resource updateResource(Long id, Resource resourceDetails) {
        User currentUser = getCurrentUser();
        Resource resource = resourceRepository.findByIdAndTenantId(id, currentUser.getTenant().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found with id: " + id));

        // Only owner or admin can update
        if (!resource.getOwner().getId().equals(currentUser.getId()) && 
            !currentUser.getRole().equals(User.Role.ADMIN)) {
            throw new UnauthorizedAccessException("You don't have permission to update this resource");
        }

        resource.setName(resourceDetails.getName());
        resource.setDescription(resourceDetails.getDescription());
        
        // Only admin can change owner
        if (currentUser.getRole().equals(User.Role.ADMIN)) {
            if (resourceDetails.getOwner() != null && 
                !resourceDetails.getOwner().getId().equals(resource.getOwner().getId())) {
                User newOwner = userRepository.findByIdAndTenantId(
                        resourceDetails.getOwner().getId(), 
                        currentUser.getTenant().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("New owner not found"));
                resource.setOwner(newOwner);
            }
        }

        return resourceRepository.save(resource);
    }

    // Delete resource (soft delete)
    @Transactional
    public void deleteResource(Long id) {
        User currentUser = getCurrentUser();
        Resource resource = resourceRepository.findByIdAndTenantId(id, currentUser.getTenant().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found with id: " + id));

        // Only owner or admin can delete
        if (!resource.getOwner().getId().equals(currentUser.getId()) && 
            !currentUser.getRole().equals(User.Role.ADMIN)) {
            throw new UnauthorizedAccessException("You don't have permission to delete this resource");
        }

        resourceRepository.softDelete(id, currentUser.getTenant().getId());
    }

    // Search resources with filters
    public Page<Resource> searchResources(String name, String description, Pageable pageable) {
        User currentUser = getCurrentUser();
        return resourceRepository.searchResources(
            currentUser.getTenant().getId(), 
            name, 
            description, 
            pageable);
    }

    // Helper method to get current authenticated user
    private User getCurrentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    // Validate resource quotas
    private void validateResourceQuotas(User user) {
        // Check user resource limit (10 per user)
        long userResourceCount = resourceRepository.countByOwnerId(user.getId());
        if (userResourceCount >= 10) {
            throw new QuotaExceededException("User cannot have more than 10 resources");
        }
        
        // Check tenant resource limit (500 per tenant)
        long tenantResourceCount = resourceRepository.countByTenantId(user.getTenant().getId());
        if (tenantResourceCount >= 500) {
            throw new QuotaExceededException("Tenant cannot have more than 500 resources");
        }
    }
}