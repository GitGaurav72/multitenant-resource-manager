package com.edstruments.multitenant_resource_manager.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SchemaManagementService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Transactional
    public void createSchemaIfNotExists(String schemaName) {
        // In Oracle, we create a user (which acts as a schema)
        // Note: This requires additional privileges for the application user
        
        // First check if user/schema already exists
        if (!userExists(schemaName)) {
            // Create user with password (you might want to generate a secure password)
            String password = generateSecurePassword(schemaName);
            jdbcTemplate.execute("CREATE USER " + schemaName + " IDENTIFIED BY \"" + password + "\"");
            
            // Grant basic privileges
            jdbcTemplate.execute("GRANT CONNECT, RESOURCE TO " + schemaName);
            jdbcTemplate.execute("GRANT CREATE SESSION TO " + schemaName);
            jdbcTemplate.execute("GRANT UNLIMITED TABLESPACE TO " + schemaName);
        }
        
        // Alternatively, if you just want to use existing schemas without creating users:
        // jdbcTemplate.execute("CREATE SCHEMA AUTHORIZATION " + schemaName);
    }

    @Transactional
    public void createSchemaWithPrivileges(String schemaName, String password) {
        if (!userExists(schemaName)) {
            jdbcTemplate.execute("CREATE USER " + schemaName + " IDENTIFIED BY \"" + password + "\"");
            jdbcTemplate.execute("GRANT CONNECT, RESOURCE TO " + schemaName);
            jdbcTemplate.execute("GRANT CREATE SESSION TO " + schemaName);
            jdbcTemplate.execute("GRANT UNLIMITED TABLESPACE TO " + schemaName);
            jdbcTemplate.execute("GRANT CREATE TABLE TO " + schemaName);
            jdbcTemplate.execute("GRANT CREATE SEQUENCE TO " + schemaName);
            jdbcTemplate.execute("GRANT CREATE VIEW TO " + schemaName);
        }
    }

    @Transactional
    public void dropSchema(String schemaName) {
        // Drop user and all their objects
        jdbcTemplate.execute("DROP USER " + schemaName + " CASCADE");
    }

    public boolean schemaExists(String schemaName) {
        // In Oracle, check if user exists
        return userExists(schemaName);
    }

    private boolean userExists(String username) {
        try {
            String sql = "SELECT COUNT(*) FROM all_users WHERE username = UPPER(?)";
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class, username.toUpperCase());
            return count != null && count > 0;
        } catch (Exception e) {
            return false;
        }
    }

    @Transactional
    public void createTablesInSchema(String schemaName) {
        // Switch to the schema context
        String currentSchema = getCurrentSchema();
        
        try {
            // Set current schema (user)
            jdbcTemplate.execute("ALTER SESSION SET CURRENT_SCHEMA = " + schemaName);
            
            // Create tables in the schema
            createUserTable();
            // Add more table creation methods as needed
            
        } finally {
            // Restore original schema
            jdbcTemplate.execute("ALTER SESSION SET CURRENT_SCHEMA = " + currentSchema);
        }
    }

    private void createUserTable() {
        String createUserTable = "CREATE TABLE users (" +
            "id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY, " +
            "name VARCHAR2(255) NOT NULL, " +
            "email VARCHAR2(255) UNIQUE NOT NULL, " +
            "created_date TIMESTAMP DEFAULT SYSTIMESTAMP" +
            ")";
        
        jdbcTemplate.execute(createUserTable);
        
        // Create index
        jdbcTemplate.execute("CREATE INDEX idx_users_email ON users(email)");
    }

    public String getCurrentSchema() {
        try {
            return jdbcTemplate.queryForObject(
                "SELECT SYS_CONTEXT('USERENV', 'CURRENT_SCHEMA') FROM DUAL", 
                String.class
            );
        } catch (Exception e) {
            return null;
        }
    }

    @Transactional
    public void grantSchemaAccess(String schemaName, String targetUser) {
        // Grant access to objects in the schema to another user
        jdbcTemplate.execute("GRANT SELECT, INSERT, UPDATE, DELETE ON " + 
                           schemaName + ".users TO " + targetUser);
    }

    private String generateSecurePassword(String schemaName) {
        // Generate a secure password - in production, use a proper password generator
        return schemaName + "_Pwd123!"; // Example - use a proper password generator
    }

    @Transactional
    public void resetSchemaPassword(String schemaName, String newPassword) {
        jdbcTemplate.execute("ALTER USER " + schemaName + " IDENTIFIED BY \"" + newPassword + "\"");
    }

    public boolean validateSchemaConnection(String schemaName, String password) {
        // This would require a separate connection test
        // For simplicity, we'll just check if the user exists
        return userExists(schemaName);
    }
}