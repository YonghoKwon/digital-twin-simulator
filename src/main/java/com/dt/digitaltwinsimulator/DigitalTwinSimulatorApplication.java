package com.dt.digitaltwinsimulator;

import com.dt.digitaltwinsimulator.logic.DynamicDataSourceLogic;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.EnableAsync;

import javax.sql.DataSource;

@SpringBootApplication
@EnableAsync
public class DigitalTwinSimulatorApplication {

    public static void main(String[] args) {
        SpringApplication.run(DigitalTwinSimulatorApplication.class, args);
    }

//    @Bean
//    @Primary
//    public DataSource dataSource(DynamicDataSourceLogic dynamicDataSourcelogic) {
//        return dynamicDataSourcelogic.getDataSource();
//    }
}
