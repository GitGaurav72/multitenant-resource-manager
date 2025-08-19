package com.edstruments.multitenant_resource_manager.entity;


import java.util.Collections;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.edstruments.multitenant_resource_manager.security.UserPrincipal;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @ManyToOne
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    public enum Role {
        ADMIN, MANAGER, EMPLOYEE, SUPER_ADMIN
    }

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
	}

	public Tenant getTenant() {
		return tenant;
	}

	public void setTenant(Tenant tenant) {
		this.tenant = tenant;
	}

	public boolean isDeleted() {
		return isDeleted;
	}

	public void setDeleted(boolean isDeleted) {
		this.isDeleted = isDeleted;
	}

	public User(Long id, String username, String password, Role role, Tenant tenant, boolean isDeleted) {
		super();
		this.id = id;
		this.username = username;
		this.password = password;
		this.role = role;
		this.tenant = tenant;
		this.isDeleted = isDeleted;
	}

	public User(Long id, String username, String password,Tenant tenant, Role role) {
		super();
		this.id = id;
		this.username = username;
		this.password = password;
		this.role = role;
		this.tenant = tenant;
	}
	public User() {
		super();
	}

    // Getters and Setters
	
    
}