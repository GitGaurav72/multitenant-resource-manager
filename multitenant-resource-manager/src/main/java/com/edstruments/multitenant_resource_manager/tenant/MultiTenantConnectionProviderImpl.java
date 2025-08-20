package com.edstruments.multitenant_resource_manager.tenant;

import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.hibernate.service.UnknownUnwrapTypeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

@Component
public class MultiTenantConnectionProviderImpl implements MultiTenantConnectionProvider {

    @Autowired
    private DataSource dataSource;

    @Override
    public Connection getAnyConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    public void releaseAnyConnection(Connection connection) throws SQLException {
        connection.close();
    }


    @Override
    public boolean supportsAggressiveRelease() {
        return true;
    }

    @Override
    public boolean isUnwrappableAs(Class<?> unwrapType) {
        return MultiTenantConnectionProvider.class.isAssignableFrom(unwrapType) ||
               DataSource.class.isAssignableFrom(unwrapType);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T unwrap(Class<T> unwrapType) {
        if (MultiTenantConnectionProvider.class.isAssignableFrom(unwrapType)) {
            return (T) this;
        } else if (DataSource.class.isAssignableFrom(unwrapType)) {
            return (T) dataSource;
        } else {
            throw new UnknownUnwrapTypeException(unwrapType);
        }
    }

	@Override
	public Connection getConnection(Object tenantIdentifier) throws SQLException {
		 Connection connection = getAnyConnection();
	        try {
	            // For Oracle, we need to change the current schema
	            try (Statement statement = connection.createStatement()) {
	                statement.execute("ALTER SESSION SET CURRENT_SCHEMA = " + tenantIdentifier);
	            }
	        } catch (SQLException e) {
	            throw new RuntimeException("Failed to set schema to: " + tenantIdentifier, e);
	        }
	        return connection;
	}

	@Override
	public void releaseConnection(Object tenantIdentifier, Connection connection) throws SQLException {
		 try {
	            // Reset to default schema
	            try (Statement statement = connection.createStatement()) {
	                String defaultSchema = getDefaultSchema(); // You need to implement this
	                statement.execute("ALTER SESSION SET CURRENT_SCHEMA = " + defaultSchema);
	            }
	        } catch (SQLException e) {
	            System.err.println("Warning: Failed to reset schema: " + e.getMessage());
	        } finally {
	            connection.close();
	        }
		
	}
	
	public String getDefaultSchema()  {
			return "SYSTEM";
	}
}