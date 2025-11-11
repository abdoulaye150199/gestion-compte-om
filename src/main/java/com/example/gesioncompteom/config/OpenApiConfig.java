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

        // ✅ Définir le schéma de sécurité JWT Bearer
        SecurityScheme bearerAuthScheme = new SecurityScheme()
                .name("Authorization")
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER);

        // ✅ Ajouter une exigence de sécurité globale
        SecurityRequirement securityRequirement = new SecurityRequirement().addList("bearerAuth");

        // ✅ Définir les serveurs (local + production Render)
        List<Server> servers = new ArrayList<>();
        servers.add(new Server().url("http://localhost:8080").description("Local Development"));
        servers.add(new Server().url("https://gestion-compte-om-1.onrender.com").description("Production Render 1"));
        servers.add(new Server().url("https://gestion-compte-om-2.onrender.com").description("Production Render 2"));

        // ✅ Construire la documentation OpenAPI
        return new OpenAPI()
                .components(new Components().addSecuritySchemes("bearerAuth", bearerAuthScheme))
                .addSecurityItem(securityRequirement)
                .info(new Info()
                        .title("Gestion Compte OM API")
                        .version("1.0.0")
                        .description("API de gestion des comptes et transactions")
                        .contact(new Contact()
                                .name("Abdoulaye Diallo")
                                .email("abdoulaye@example.com")
                        )
                )
                .servers(servers);
    }
}
