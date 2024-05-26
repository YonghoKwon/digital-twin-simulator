package com.dt.digitaltwinsimulator.logic;

import com.dt.digitaltwinsimulator.entity.dto.DBConnectionInfoDto;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.bind.Name;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

@Getter
@Slf4j
@Service
public class DynamicDataSourceLogic {

    private final DataSource defaultDataSource;
    private DataSource dynamicDataSource;
    private final HikariDataSource dataSource;

    public DynamicDataSourceLogic(
            @Qualifier("defaultDataSource") DataSource defaultDataSource,
            @Qualifier("dynamicDataSource") DataSource dynamicDataSource,
            HikariDataSource dataSource
    ) {
        this.defaultDataSource = defaultDataSource;
        this.dynamicDataSource = dynamicDataSource;
        this.dataSource = dataSource;
    }

    public boolean setDataSource(String db, DBConnectionInfoDto dbConnectionInfoDto) {
        HikariPoolMXBean poolMXBean = dataSource.getHikariPoolMXBean();

        log.info("Before Active Connections: {}", poolMXBean.getActiveConnections());
        log.info("Before Idle Connections: {}", poolMXBean.getIdleConnections());
        log.info("Before Total Connections: {}", poolMXBean.getTotalConnections());
        log.info("Before Threads Awaiting Connection: {}", poolMXBean.getThreadsAwaitingConnection());

        try {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(dbConnectionInfoDto.getUrl());
            config.setUsername(dbConnectionInfoDto.getUsername());
            config.setPassword(dbConnectionInfoDto.getPassword());
            config.setDriverClassName(dbConnectionInfoDto.getDriverClassName());

            HikariDataSource newHikariDataSource = new HikariDataSource(config);

            if(newHikariDataSource.isRunning()) {
                HikariDataSource oldHikariDataSource = (HikariDataSource) this.dynamicDataSource;
                oldHikariDataSource.close();
                this.dynamicDataSource = newHikariDataSource;
            }

            log.info("After Active Connections: {}", poolMXBean.getActiveConnections());
            log.info("After Idle Connections: {}", poolMXBean.getIdleConnections());
            log.info("After Total Connections: {}", poolMXBean.getTotalConnections());
            log.info("After Threads Awaiting Connection: {}", poolMXBean.getThreadsAwaitingConnection());

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean testConnection() {
        try (Connection connection = this.dynamicDataSource.getConnection();
             Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery("SELECT 1");

//            ResultSet resultSet1 = statement.executeQuery("SELECT * FROM testdb.db_connection");
//            while (resultSet1.next()) {
//                log.info("id: {}, name: {}", resultSet1.getInt("id"), resultSet1.getString("username"));
//            }

            return resultSet.next();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
