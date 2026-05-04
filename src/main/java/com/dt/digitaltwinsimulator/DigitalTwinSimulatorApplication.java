package com.dt.digitaltwinsimulator;

import com.dt.digitaltwinsimulator.config.HttpLoggingProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
@EnableConfigurationProperties(HttpLoggingProperties.class)
public class DigitalTwinSimulatorApplication {

    public static void main(String[] args) {
        SpringApplication.run(DigitalTwinSimulatorApplication.class, args);
    }
}
