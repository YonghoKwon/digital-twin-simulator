package com.dt.activemqsimulator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class ActivemqSimulatorApplication {

    public static void main(String[] args) {
        SpringApplication.run(ActivemqSimulatorApplication.class, args);
    }

}
