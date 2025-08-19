package com.edstruments.multitenant_resource_manager.security;

import com.edstruments.multitenant_resource_manager.exception.ResourceNotFoundException;
import com.edstruments.multitenant_resource_manager.entity.User;
import com.edstruments.multitenant_resource_manager.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

	@Autowired
    private UserRepository userRepository;


    @Override
    @Transactional
    public UserDetails loadUserByUsername(String usernameWithTenant) 
            throws UsernameNotFoundException {
        // Split the combined username and tenant from the login request
        String[] parts = usernameWithTenant.split("\\|");
        if (parts.length != 2) {
            throw new UsernameNotFoundException(
                "Invalid username format. Expected 'username|tenantId'");
        }
        
        String username = parts[0];
        Long tenantId;
        
        try {
            tenantId = Long.parseLong(parts[1]);
        } catch (NumberFormatException e) {
            throw new UsernameNotFoundException("Tenant ID must be a number");
        }

        User user = userRepository.findByUsernameAndTenantId(username, tenantId)
                .orElseThrow(() -> new UsernameNotFoundException(
                    String.format("User not found with username: %s in tenant: %d", 
                                username, tenantId)));

        if (user.isDeleted()) {
            throw new UsernameNotFoundException(
                String.format("User with username: %s in tenant: %d is deleted", 
                            username, tenantId));
        }

        return UserPrincipal.create(user);
    }

    @Transactional
    public UserDetails loadUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        if (user.isDeleted()) {
            throw new ResourceNotFoundException("User", "id", id);
        }

        return UserPrincipal.create(user);
    }
}