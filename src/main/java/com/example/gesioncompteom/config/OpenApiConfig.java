package com.example.gestioncompteom.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.ArrayList;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        SecurityScheme bearer = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER)
                .name("Authorization");

        return new OpenAPI()
                .components(new Components().addSecuritySchemes("bearerAuth", bearer))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .info(new Info()
                        .title("Gestion Compte OM API")
                        .version("1.0.0")
                        .description("API de gestion des comptes et transactions")
                        .contact(new Contact()
                                .name("Abdoulaye Diallo")
                                .email("abdoulaye@example.com")
                        )
                )
                .servers(new ArrayList<>(List.of(
                        new Server().url("http://localhost:8080").description("Local development"),
                        new Server().url("https://gestion-compte-om-1.onrender.com").description("Production Render 1"),
                        new Server().url("https://gestion-compte-om-2.onrender.com").description("Production Render 2")
                )));
    }
}
