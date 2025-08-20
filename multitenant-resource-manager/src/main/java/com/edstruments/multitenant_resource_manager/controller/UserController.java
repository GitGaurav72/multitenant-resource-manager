package com.edstruments.multitenant_resource_manager.controller;

import com.edstruments.multitenant_resource_manager.entity.User;
import com.edstruments.multitenant_resource_manager.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

	@Autowired
    private UserService userService;


    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> createUser(@Valid @RequestBody User user,@RequestHeader("X-TenantID") String tenantId) {
        User createdUser = userService.createUser(user);
        
        URI location = ServletUriComponentsBuilder
            .fromCurrentContextPath().path("/api/users/{id}")
            .buildAndExpand(createdUser.getId()).toUri();
            
        return ResponseEntity.created(location).body(createdUser);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable("id") Long id,@RequestHeader("X-TenantID") String tenantId) {
        userService.deleteUser(id);
        return ResponseEntity.ok().body("User deleted successfully");
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }
}