package com.debate.croll.datasource;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import jakarta.persistence.EntityManagerFactory;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
	basePackages = "com.debate.croll.logger",
	transactionManagerRef = "loggerTransactionManager",
	entityManagerFactoryRef = "loggerEntityManagerFactory")
@EntityScan("com.debate.croll.logger")
public class LoggerConfig {

	@Bean(name = "loggerDataSource")
	@ConfigurationProperties(prefix = "spring.datasource.logger")
	public DataSource loggerDataSource() {
		return DataSourceBuilder.create().build();
	}

	@Bean(name = "loggerEntityManagerFactoryBuilder")
	public EntityManagerFactoryBuilder loggerEntityManagerFactoryBuilder() {
		return new EntityManagerFactoryBuilder(new HibernateJpaVendorAdapter(), new HashMap<>(), null);
	}

	@Bean(name = "loggerEntityManagerFactory")
	public LocalContainerEntityManagerFactoryBean loggerEntityManagerFactory(
		@Qualifier("loggerEntityManagerFactoryBuilder") EntityManagerFactoryBuilder builder,
		@Qualifier("loggerDataSource") DataSource dataSource) {
		Map<String, Object> jpaProperties = new HashMap<>();
		jpaProperties.put("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
		jpaProperties.put("hibernate.hbm2ddl.auto", "update");
		jpaProperties.put("hibernate.format_sql", true);
		jpaProperties.put("hibernate.show-sql", true);

		return builder
			.dataSource(dataSource)
			.properties(jpaProperties)
			.packages("com.debate.croll.logger")
			.persistenceUnit("logger")
			.build();
	}

	@Bean(name = "loggerTransactionManager")
	public PlatformTransactionManager loggerTransactionManager(
		@Qualifier("loggerEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
		JpaTransactionManager transactionManager = new JpaTransactionManager();
		transactionManager.setEntityManagerFactory(entityManagerFactory);
		return transactionManager;
	}


}
