package com.edstruments.multitenant_resource_manager.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DatabaseSchemaService {

    private final JdbcTemplate jdbcTemplate;

    public DatabaseSchemaService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

//    @Transactional
//    public void createSchema(String schemaName) {
//        // Create the schema
//        jdbcTemplate.execute("CREATE SCHEMA IF NOT EXISTS " + schemaName);
//
//        // Create tables in the new schema
//        createUserTable(schemaName);
//        createResourceTable(schemaName);
//        createAuditLogTable(schemaName);
//    }
    
    @Transactional
    public void createSchema(String schemaName, String password) {
        try {
            // Create User (Schema)
            jdbcTemplate.execute("CREATE USER " + schemaName + " IDENTIFIED BY " + password);
            
            // Grant minimum privileges
            jdbcTemplate.execute("GRANT CONNECT, RESOURCE TO " + schemaName);
            
            // Optionally grant quota for tablespace
            jdbcTemplate.execute("ALTER USER " + schemaName + " QUOTA UNLIMITED ON USERS");
            
        } catch (Exception e) {
            if (e.getMessage().contains("ORA-01920")) {
                // ORA-01920: user name conflicts with another existing user
                System.out.println("Schema already exists: " + schemaName);
            } else {
                throw e;
            }
        }

        // Create tables in the new schema
        Integer count = jdbcTemplate.queryForObject(
        	    "SELECT COUNT(*) FROM all_tables WHERE owner = ? AND table_name = ?",
        	    Integer.class,
        	    schemaName.toUpperCase(),
        	    "USERS"
        	);

        	if (count == 0) {
        	    createUserTable(schemaName);
        	}
        createResourceTable(schemaName);
        createAuditLogTable(schemaName);
    }


    private void createUserTable(String schemaName) {
        jdbcTemplate.execute("CREATE TABLE " + schemaName + ".users (" +
                "id NUMBER(19,0) PRIMARY KEY, " +
                "username VARCHAR2(255) NOT NULL, " +
                "password VARCHAR2(255) NOT NULL, " +
                "role VARCHAR2(50) NOT NULL, " +
                "tenant_id NUMBER(19,0) NOT NULL, " +
                "is_deleted NUMBER(1) DEFAULT 0 NOT NULL, " +
                "CONSTRAINT uq_" + schemaName + "_username_tenant UNIQUE (username, tenant_id))");
    }


    private void createResourceTable(String schemaName) {
        String sql = String.format("""
            CREATE TABLE %s.resources (
                id NUMBER(19,0) PRIMARY KEY,
                name VARCHAR2(255) NOT NULL,
                description VARCHAR2(255),
                owner_id NUMBER(19,0) NOT NULL,
                tenant_id NUMBER(19,0) NOT NULL,
                is_deleted NUMBER(1) DEFAULT 0 NOT NULL,
                CONSTRAINT fk_%s_resources_owner FOREIGN KEY (owner_id) REFERENCES %s.users(id)
            )
            """, schemaName, schemaName.toLowerCase(), schemaName);
        jdbcTemplate.execute(sql);
    }

    private void createAuditLogTable(String schemaName) {
        String sql = String.format("""
            CREATE TABLE %s.audit_logs (
                id NUMBER(19,0) PRIMARY KEY,
                user_id NUMBER(19,0) NOT NULL,
                action VARCHAR2(255) NOT NULL,
                event_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
                CONSTRAINT fk_%s_auditlogs_user FOREIGN KEY (user_id) REFERENCES %s.users(id)
            )
            """, schemaName, schemaName.toLowerCase(), schemaName);
        jdbcTemplate.execute(sql);
    }

    @Transactional
    public void dropSchema(String schemaName) {
        // Drop the schema (use with caution - this deletes all data)
        jdbcTemplate.execute("DROP SCHEMA IF EXISTS " + schemaName + " CASCADE");
    }

    public boolean schemaExists(String schemaName) {
        String sql = """
            SELECT EXISTS (
                SELECT 1 FROM information_schema.schemata 
                WHERE schema_name = ?
            )
            """;
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, Boolean.class, schemaName));
    }
}