package com.edstruments.multitenant_resource_manager.util;

import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.edstruments.multitenant_resource_manager.entity.User;
import com.edstruments.multitenant_resource_manager.repository.TenantRepository;
import com.edstruments.multitenant_resource_manager.security.UserPrincipal;

public class commonHelper {

	@Autowired
	private static TenantRepository tenantRepository;
	
	
	 public static User create(UserPrincipal userPrincipal) {
	        return new User(
	        		userPrincipal.getId(),
	        		userPrincipal.getUsername(),
	        		userPrincipal.getPassword(),
	        		tenantRepository.getById(userPrincipal.getTenantId()),
	        		userPrincipal.getRole()
	        );
	    }
}
