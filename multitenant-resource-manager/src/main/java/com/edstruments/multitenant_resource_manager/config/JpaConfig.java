package com.edstruments.multitenant_resource_manager.config;

import com.edstruments.multitenant_resource_manager.tenant.MultiTenantConnectionProviderImpl;
import com.edstruments.multitenant_resource_manager.tenant.TenantIdentifierResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class JpaConfig {

    @Autowired
    private MultiTenantConnectionProviderImpl multiTenantConnectionProvider;

    @Autowired
    private TenantIdentifierResolver tenantIdentifierResolver;

    @Bean
    public JpaVendorAdapter jpaVendorAdapter() {
        return new HibernateJpaVendorAdapter();
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setPackagesToScan("com.edstruments.multitenant_resource_manager.entity");
        em.setJpaVendorAdapter(jpaVendorAdapter());

        Map<String, Object> jpaProperties = new HashMap<>();
//        jpaProperties.put("hibernate.multiTenancy", MultiTenancyStrategy.SCHEMA.name());
        jpaProperties.put("hibernate.multi_tenant_connection_provider", multiTenantConnectionProvider);
        jpaProperties.put("hibernate.tenant_identifier_resolver", tenantIdentifierResolver);
        jpaProperties.put("hibernate.dialect", "org.hibernate.dialect.OracleDialect");
        jpaProperties.put("hibernate.hbm2ddl.auto", "update"); // or "validate", "create", etc.
        jpaProperties.put("hibernate.show_sql", true);
        jpaProperties.put("hibernate.format_sql", true);

        em.setJpaPropertyMap(jpaProperties);
        return em;
    }
}