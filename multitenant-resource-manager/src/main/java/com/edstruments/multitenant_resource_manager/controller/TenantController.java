package com.edstruments.multitenant_resource_manager.controller;

import com.edstruments.multitenant_resource_manager.entity.Tenant;
import com.edstruments.multitenant_resource_manager.service.TenantService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import jakarta.persistence.EntityNotFoundException;
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

//    @DeleteMapping("/{id}")
//    @PreAuthorize("hasRole('SUPER_ADMIN')")
//    public ResponseEntity<?> deleteTenant(@PathVariable Long id) {
//        tenantService.deleteTenant(id);
//        return ResponseEntity.ok().body("Tenant deleted successfully");
//    }
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> deleteTenant(@PathVariable("id") Long id) {
        try {
            tenantService.deleteTenant(id);
            return ResponseEntity.ok().body("Tenant deleted successfully");
        } catch (EntityNotFoundException e) {
            // If tenant not found
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Tenant with ID " + id + " not found");
        } catch (DataIntegrityViolationException e) {
            // If foreign key constraint prevents deletion
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Cannot delete tenant because it is referenced by other records");
        } catch (Exception e) {
            // For any other unexpected errors
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while deleting tenant: " + e.getMessage());
        }
    }

}