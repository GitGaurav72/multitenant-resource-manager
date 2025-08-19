package com.edstruments.multitenant_resource_manager.service;

import com.edstruments.multitenant_resource_manager.exception.QuotaExceededException;
import com.edstruments.multitenant_resource_manager.exception.ResourceNotFoundException;
import com.edstruments.multitenant_resource_manager.exception.UnauthorizedAccessException;
import com.edstruments.multitenant_resource_manager.entity.Tenant;
import com.edstruments.multitenant_resource_manager.entity.User;
import com.edstruments.multitenant_resource_manager.repository.TenantRepository;
import com.edstruments.multitenant_resource_manager.repository.UserRepository;
import com.edstruments.multitenant_resource_manager.security.UserPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;

    public UserService(UserRepository userRepository, 
                      TenantRepository tenantRepository, 
                      PasswordEncoder passwordEncoder,
                      AuditLogService auditLogService) {
        this.userRepository = userRepository;
        this.tenantRepository = tenantRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public User createUser(User user) {
        User currentUser = getCurrentUser();
        Tenant currentTenant = currentUser.getTenant();

        // Validate user quota (50 users per tenant)
        long userCount = userRepository.countByTenantId(currentTenant.getId());
        if (userCount >= 50) {
            throw new QuotaExceededException("User", userCount, 50);
        }

        // Check if username already exists in the same tenant
        if (userRepository.existsByUsernameAndTenantId(user.getUsername(), currentTenant.getId())) {
            throw new IllegalArgumentException("Username already exists for this tenant");
        }

        // Set tenant and encode password
        user.setTenant(currentTenant);
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        User savedUser = userRepository.save(user);
        
        // Log the action
        auditLogService.logUserAction(currentUser, "CREATED_USER", 
            "Created user: " + savedUser.getUsername());
        
        return savedUser;
    }

    @Transactional
    public void deleteUser(Long id) {
        User currentUser = getCurrentUser();
        User userToDelete = userRepository.findByIdAndTenantId(id, currentUser.getTenant().getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        // Soft delete the user
        userRepository.softDelete(id, currentUser.getTenant().getId());
        
        // Log the action
        auditLogService.logUserAction(currentUser, "DELETED_USER", 
            "Deleted user: " + userToDelete.getUsername());
    }

    public List<User> getAllUsers() {
        User currentUser = getCurrentUser();
        return userRepository.findByTenantIdAndIsDeletedFalse(currentUser.getTenant().getId());
    }

    public User getUserById(Long id) {
        User currentUser = getCurrentUser();
        return userRepository.findByIdAndTenantId(id, currentUser.getTenant().getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
    }

    @Transactional
    public User updateUser(Long id, User userDetails) {
        User currentUser = getCurrentUser();
        User user = userRepository.findByIdAndTenantId(id, currentUser.getTenant().getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        // Only allow updating certain fields
        if (userDetails.getUsername() != null) {
            // Check if new username is available
            if (!user.getUsername().equals(userDetails.getUsername()) && 
                userRepository.existsByUsernameAndTenantId(userDetails.getUsername(), currentUser.getTenant().getId())) {
                throw new IllegalArgumentException("Username already exists for this tenant");
            }
            user.setUsername(userDetails.getUsername());
        }

        if (userDetails.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(userDetails.getPassword()));
        }

        if (userDetails.getRole() != null) {
            // Only admins can change roles
            if (!currentUser.getRole().equals(User.Role.ADMIN)) {
                throw new UnauthorizedAccessException("Only admins can change user roles");
            }
            user.setRole(userDetails.getRole());
        }

        User updatedUser = userRepository.save(user);
        
        // Log the action
        auditLogService.logUserAction(currentUser, "UPDATED_USER", 
            "Updated user: " + updatedUser.getUsername());
        
        return updatedUser;
    }

    private User getCurrentUser() {
        UserPrincipal principal = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        return userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", principal.getId()));
    }
}