package com.edstruments.multitenant_resource_manager.tenant;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class TenantAwareRequestFilter implements Filter {
    
    private static final String[] TENANT_HEADERS = {"X-TenantID", "X-Tenant-ID", "Tenant-ID"};
    private static final String DEFAULT_TENANT = "public";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
            throws IOException, ServletException {
        
        HttpServletRequest req = (HttpServletRequest) request;
        String tenantId = extractTenantId(req);
        
        System.out.println("---------------------------------- Tenant ID: " + tenantId);
        
        if (!StringUtils.hasText(tenantId)) {
            throw new ServletException("Tenant ID is required. Please provide X-TenantID header");
        }
        
        TenantContext.setCurrentTenant(tenantId);
        
        try {
            chain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }

    private String extractTenantId(HttpServletRequest request) {
        // Check multiple header names
        for (String header : TENANT_HEADERS) {
            String tenantId = request.getHeader(header);
            if (StringUtils.hasText(tenantId)) {
                return tenantId.trim().toLowerCase(); // Normalize to lowercase
            }
        }
        
        // Check parameter as fallback
        String tenantParam = request.getParameter("tenant");
        if (StringUtils.hasText(tenantParam)) {
            return tenantParam.trim().toLowerCase();
        }
        
        return DEFAULT_TENANT; // Or throw exception if you don't want default
    }
}