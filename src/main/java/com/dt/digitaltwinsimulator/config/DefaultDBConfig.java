package com.dt.digitaltwinsimulator.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

@Configuration
public class DefaultDBConfig {
    @Bean(name = "defaultDataSource")
    @Primary
    public DataSource defaultDataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:h2:mem:test");
        config.setUsername("username");
        config.setPassword("password");
        config.setDriverClassName("org.h2.Driver");
        return new HikariDataSource(config);
    }

    @Bean(name = "dynamicDataSource")
    public DataSource dynamicDataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:h2:mem:test");
        config.setUsername("username");
        config.setPassword("password");
        config.setDriverClassName("org.h2.Driver");
        return new HikariDataSource(config);
    }
}
