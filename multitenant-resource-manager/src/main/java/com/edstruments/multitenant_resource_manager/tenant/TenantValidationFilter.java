package com.edstruments.multitenant_resource_manager.tenant;

import com.edstruments.multitenant_resource_manager.service.SchemaManagementService;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.Set;

@Component
public class TenantValidationFilter implements Filter {

    @Autowired
    private SchemaManagementService schemaManagementService;

    private static final Set<String> PUBLIC_ENDPOINTS = Set.of(
        "/api/health",
        "/api/tenants/register",
        "/actuator/health"
    );

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
            throws IOException, ServletException {
        
        HttpServletRequest req = (HttpServletRequest) request;
        String path = req.getRequestURI();

        // Skip validation for public endpoints
        if (PUBLIC_ENDPOINTS.stream().anyMatch(path::startsWith)) {
            chain.doFilter(request, response);
            return;
        }

        String tenantId = TenantContext.getCurrentTenant();
        
        if (!StringUtils.hasText(tenantId) || "public".equals(tenantId)) {
            sendError(response, "Valid tenant ID is required");
            return;
        }

        // Validate that the schema exists
        if (!schemaManagementService.schemaExists(tenantId)) {
            sendError(response, "Tenant schema does not exist: " + tenantId);
            return;
        }

        chain.doFilter(request, response);
    }

    private void sendError(ServletResponse response, String message) throws IOException {
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        httpResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        httpResponse.setContentType("application/json");
        httpResponse.getWriter().write("{\"error\": \"" + message + "\"}");
    }
}