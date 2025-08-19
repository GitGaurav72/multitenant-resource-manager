package com.edstruments.multitenant_resource_manager.dto;


import lombok.Data;

@Data
public class SignUpRequest {
  
    private String username;

   
    private String password;

    
    private String role; // "ADMIN", "MANAGER", or "EMPLOYEE"

    
    private Long tenantId;


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


	public String getRole() {
		return role;
	}


	public void setRole(String role) {
		this.role = role;
	}


	public Long getTenantId() {
		return tenantId;
	}


	public void setTenantId(Long tenantId) {
		this.tenantId = tenantId;
	}


	public SignUpRequest(String username, String password, String role, Long tenantId) {
		super();
		this.username = username;
		this.password = password;
		this.role = role;
		this.tenantId = tenantId;
	}


	public SignUpRequest() {
		super();
		// TODO Auto-generated constructor stub
	}
    
    
}