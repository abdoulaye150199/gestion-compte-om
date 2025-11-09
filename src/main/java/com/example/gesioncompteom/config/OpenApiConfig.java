package com.example.gesioncompteom.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Gestion Compte OM API")
                        .version("1.0.0")
                        .description("API de gestion des comptes et transactions")
                        .contact(new Contact().name("Abdoulaye Diallo").email("abdoulaye@example.com"))
                );
    }

    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("gestion-compte-om")
                .packagesToScan("com.example.gesioncompteom.controller")
                .pathsToMatch("/**")
                .build();
    }
}

