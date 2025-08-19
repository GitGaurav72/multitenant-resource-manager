package com.edstruments.multitenant_resource_manager.controller;

import com.edstruments.multitenant_resource_manager.entity.Tenant;
import com.edstruments.multitenant_resource_manager.service.TenantService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import jakarta.validation.Valid;
import java.net.URI;

@RestController
@RequestMapping("/api/tenants")
public class TenantController {

	@Autowired
    private TenantService tenantService;


    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Tenant> createTenant(@Valid @RequestBody Tenant tenant) {
        Tenant createdTenant = tenantService.createTenant(tenant);
        
        URI location = ServletUriComponentsBuilder
            .fromCurrentContextPath().path("/api/tenants/{id}")
            .buildAndExpand(createdTenant.getId()).toUri();
            
        return ResponseEntity.created(location).body(createdTenant);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> deleteTenant(@PathVariable Long id) {
        tenantService.deleteTenant(id);
        return ResponseEntity.ok().body("Tenant deleted successfully");
    }
}