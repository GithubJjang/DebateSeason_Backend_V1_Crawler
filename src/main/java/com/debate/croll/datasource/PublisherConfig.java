package com.debate.croll.datasource;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;

import jakarta.persistence.EntityManagerFactory;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
	basePackages = "com.debate.croll.publisher",
	transactionManagerRef = "publisherTransactionManager",
	entityManagerFactoryRef = "publisherEntityManagerFactory")
@EntityScan("com.debate.croll.publisher")
public class PublisherConfig {

	@Bean(name = "publisherDataSource")
	@ConfigurationProperties(prefix = "spring.datasource.publisher")
	public DataSource publisherDataSource() {
		return DataSourceBuilder.create().build();
	}

	@Bean(name = "publisherEntityManagerFactoryBuilder")
	public EntityManagerFactoryBuilder publisherEntityManagerFactoryBuilder() {
		return new EntityManagerFactoryBuilder(new HibernateJpaVendorAdapter(), new HashMap<>(), null);
	}

	@Bean(name = "publisherEntityManagerFactory")
	public LocalContainerEntityManagerFactoryBean publisherEntityManagerFactory(
		@Qualifier("publisherEntityManagerFactoryBuilder") EntityManagerFactoryBuilder builder,
		@Qualifier("publisherDataSource") DataSource dataSource) {
		Map<String, Object> jpaProperties = new HashMap<>();
		jpaProperties.put("hibernate.dialect", "org.hibernate.dialect.MariaDBDialect");
		jpaProperties.put("hibernate.hbm2ddl.auto", "update");
		jpaProperties.put("hibernate.format_sql", true);
		jpaProperties.put("hibernate.show-sql", true);

		return builder
			.dataSource(dataSource)
			.properties(jpaProperties)
			.packages("com.debate.croll.publisher")
			.persistenceUnit("publisher")
			.build();
	}

	@Bean(name = "publisherTransactionManager")
	public PlatformTransactionManager publisherTransactionManager(
		@Qualifier("publisherEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
		JpaTransactionManager transactionManager = new JpaTransactionManager();
		transactionManager.setEntityManagerFactory(entityManagerFactory);
		return transactionManager;
	}


}
