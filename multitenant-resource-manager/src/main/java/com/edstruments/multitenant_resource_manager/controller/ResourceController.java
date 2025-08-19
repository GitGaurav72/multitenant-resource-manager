package com.edstruments.multitenant_resource_manager.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.edstruments.multitenant_resource_manager.entity.Resource;
import com.edstruments.multitenant_resource_manager.exception.ResourceNotFoundException;
import com.edstruments.multitenant_resource_manager.exception.UnauthorizedAccessException;
import com.edstruments.multitenant_resource_manager.service.ResourceService;

@RestController
@RequestMapping("/api/resources")
public class ResourceController {
	@Autowired
    private ResourceService resourceService;


    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'EMPLOYEE')")
    public Page<Resource> getAllResources(Pageable pageable) {
        return resourceService.getAllResources(pageable);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public Resource createResource(@RequestBody Resource resource) {
        return resourceService.createResource(resource);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public Resource updateResource(@PathVariable Long id, @RequestBody Resource resource) {
        return resourceService.updateResource(id, resource);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public void deleteResource(@PathVariable Long id) {
    	try {
    	    resourceService.deleteResource(1L);
    	} catch (UnauthorizedAccessException ex) {
    	    // Handle permission error
    	} catch (ResourceNotFoundException ex) {
    	    // Handle not found error
    	} resourceService.deleteResource(id);
    }
}