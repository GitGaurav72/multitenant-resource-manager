package com.edstruments.multitenant_resource_manager.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.edstruments.multitenant_resource_manager.dto.LoginRequest;
import com.edstruments.multitenant_resource_manager.dto.LoginResponse;
import com.edstruments.multitenant_resource_manager.dto.SignUpRequest;
import com.edstruments.multitenant_resource_manager.exception.ResourceNotFoundException;
import com.edstruments.multitenant_resource_manager.entity.User;
import com.edstruments.multitenant_resource_manager.repository.TenantRepository;
import com.edstruments.multitenant_resource_manager.repository.UserRepository;
import com.edstruments.multitenant_resource_manager.security.JwtTokenProvider;

import java.net.URI;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	@Autowired
    private AuthenticationManager authenticationManager;
	@Autowired
    private JwtTokenProvider tokenProvider;
	@Autowired
    private UserRepository userRepository;
    @Autowired
    private TenantRepository tenanatRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> authenticateUser( @RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                loginRequest.getUsername(),
                loginRequest.getPassword()
            )
        );

        String jwt = tokenProvider.generateToken(authentication);
        return ResponseEntity.ok(new LoginResponse(jwt));
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@RequestBody SignUpRequest signUpRequest) {
        if (userRepository.existsByUsernameAndTenantId(signUpRequest.getUsername(), signUpRequest.getTenantId())) {
            return ResponseEntity.badRequest().body("Username is already taken for this tenant!");
        }

        User user = new User();
        user.setUsername(signUpRequest.getUsername());
        user.setPassword(passwordEncoder.encode(signUpRequest.getPassword())); // Password will be encoded in service
        user.setRole(User.Role.valueOf(signUpRequest.getRole()));
        user.setTenant(tenanatRepository.getById(signUpRequest.getTenantId()));
        
        User result = userRepository.save(user);
        
        URI location = ServletUriComponentsBuilder
            .fromCurrentContextPath().path("/api/users/{id}")
            .buildAndExpand(result.getId()).toUri();

        return ResponseEntity.created(location).body("User registered successfully");
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        
        return ResponseEntity.ok(user);
    }
}