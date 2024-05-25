package com.dt.digitaltwinsimulator.config;

import com.dt.digitaltwinsimulator.logic.DynamicDataSourceLogic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Configuration
@EnableJpaRepositories(
        basePackages = "com.dt.digitaltwinsimulator.repository",
        entityManagerFactoryRef = "dynamicEntityManagerFactory",
        transactionManagerRef = "dynamicTransactionManager"
)
public class DBConfig {
    private final DynamicDataSourceLogic dynamicDataSourceLogic;

    public DBConfig(DynamicDataSourceLogic dynamicDataSourceLogic) {
        this.dynamicDataSourceLogic = dynamicDataSourceLogic;
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean dynamicEntityManagerFactory(DataSource defaultDataSource) {
        LocalContainerEntityManagerFactoryBean factoryBean = new LocalContainerEntityManagerFactoryBean();
//        factoryBean.setDataSource(dynamicDataSourceLogic.getDataSource());
        factoryBean.setDataSource(Optional.ofNullable(dynamicDataSourceLogic.getDataSource()).orElse(defaultDataSource));
        factoryBean.setPackagesToScan("com.dt.digitaltwinsimulator.entity");
        factoryBean.setJpaVendorAdapter(new HibernateJpaVendorAdapter());

        Map<String, Object> jpaProperties = new HashMap<>();
        jpaProperties.put("hibernate.hbm2ddl.auto", "update");
        jpaProperties.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect"); // PostgreSQL dialect 설정
        factoryBean.setJpaPropertyMap(jpaProperties);

        return factoryBean;
    }

    @Bean
    public PlatformTransactionManager dynamicTransactionManager(EntityManagerFactory dynamicEntityManagerFactory) {
        return new JpaTransactionManager(dynamicEntityManagerFactory);
    }
}
