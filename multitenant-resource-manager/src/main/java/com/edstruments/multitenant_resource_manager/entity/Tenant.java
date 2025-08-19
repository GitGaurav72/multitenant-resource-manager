package com.edstruments.multitenant_resource_manager.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "tenants")
public class Tenant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false, unique = true)
    private String schemaName;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSchemaName() {
		return schemaName;
	}

	public void setSchemaName(String schemaName) {
		this.schemaName = schemaName;
	}

	public Tenant(Long id, String name, String schemaName) {
		super();
		this.id = id;
		this.name = name;
		this.schemaName = schemaName;
	}

	public Tenant() {
		super();
		// TODO Auto-generated constructor stub
	}

    
}