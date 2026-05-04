package com.dt.digitaltwinsimulator.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI digitalTwinDataGeneratorOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Digital Twin Data Generator API")
                        .description("API for generating bulk formatted data and publishing it to ActiveMQ Artemis topics.")
                        .version("v1.0.0")
                        .contact(new Contact().name("Digital Twin Data Generator"))
                        .license(new License().name("Internal Use")))
                .servers(List.of(new Server().url("/").description("Current server")));
    }
}
