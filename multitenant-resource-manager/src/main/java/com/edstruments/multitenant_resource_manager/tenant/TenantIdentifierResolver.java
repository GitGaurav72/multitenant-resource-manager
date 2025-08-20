package com.edstruments.multitenant_resource_manager.tenant;

import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class TenantIdentifierResolver implements CurrentTenantIdentifierResolver {

    @Override
    public String resolveCurrentTenantIdentifier() {
        String tenantId = TenantContext.getCurrentTenant();
        if (StringUtils.hasText(tenantId)) {
            return tenantId;
        }
        // Return a default tenant or throw exception
        throw new IllegalStateException("No tenant identifier specified in current context");
    }

    @Override
    public boolean validateExistingCurrentSessions() {
        return true;
    }
}