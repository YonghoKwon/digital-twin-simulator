package com.dt.digitaltwinsimulator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class DigitalTwinSimulatorApplication {

    public static void main(String[] args) {
        SpringApplication.run(DigitalTwinSimulatorApplication.class, args);
    }

}
