package com.dt.digitaltwinsimulator.logic;

import com.dt.digitaltwinsimulator.config.DynamicRoutingDataSource;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import com.dt.digitaltwinsimulator.entity.dto.DBConnectionInfoDto;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

@Getter
@Slf4j
@Service
public class DynamicDataSourceLogic {

    private final DataSource dataSource;
    private final DynamicRoutingDataSource dynamicRoutingDataSource;

    public DynamicDataSourceLogic(DataSource dataSource, DynamicRoutingDataSource dynamicRoutingDataSource) {
        this.dataSource = dataSource;
        this.dynamicRoutingDataSource = dynamicRoutingDataSource;
    }

    public boolean setDataSource(String db, DBConnectionInfoDto dbConnectionInfoDto) {
        try {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(dbConnectionInfoDto.getUrl());
            config.setUsername(dbConnectionInfoDto.getUsername());
            config.setPassword(dbConnectionInfoDto.getPassword());
            config.setDriverClassName("org.postgresql.Driver");

            HikariDataSource newDataSource = new HikariDataSource(config);

            Map<Object, Object> targetDataSources = new HashMap<>(dynamicRoutingDataSource.getResolvedDataSources());
            targetDataSources.put("dynamicDataSource", newDataSource);
            dynamicRoutingDataSource.setTargetDataSources(targetDataSources);
            dynamicRoutingDataSource.afterPropertiesSet();

            DynamicRoutingDataSource.setDataSourceKey("dynamicDataSource");

//            HikariConfig config = new HikariConfig();
//            config.setJdbcUrl(dbConnectionInfoDto.getUrl());
//            config.setUsername(dbConnectionInfoDto.getUsername());
//            config.setPassword(dbConnectionInfoDto.getPassword());
//
//            if(db.equals("H2")) {
//                config.setDriverClassName("org.h2.Driver"); // H2 드라이버 설정
//            } else if(db.equals("PostgreSQL")) {
//                config.setDriverClassName("org.postgresql.Driver"); // PostgreSQL 드라이버 설정
//            }
//
//            this.dataSource = new HikariDataSource(config);

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean testConnection() {
        try (Connection connection = dynamicRoutingDataSource.getConnection();
             Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery("SELECT 1");

            ResultSet resultSet1 = statement.executeQuery("SELECT * FROM testdb.db_connection");
            while (resultSet1.next()) {
                log.info("id: {}, name: {}", resultSet1.getInt("id"), resultSet1.getString("username"));
            }

            return resultSet.next();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
