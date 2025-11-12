package com.example.gestioncompteom.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;

import java.util.List;
import java.util.ArrayList;
// no OpenApiCustomiser import (not available on current classpath)

@Configuration
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER
)
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {

        // ✅ Définir le schéma de sécurité JWT Bearer (modèle OpenAPI)
        io.swagger.v3.oas.models.security.SecurityScheme bearerAuthScheme = new io.swagger.v3.oas.models.security.SecurityScheme()
                .name("Authorization")
                .type(io.swagger.v3.oas.models.security.SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(io.swagger.v3.oas.models.security.SecurityScheme.In.HEADER);

        // ✅ Ajouter une exigence de sécurité globale
        SecurityRequirement securityRequirement = new SecurityRequirement().addList("bearerAuth");

        // ✅ Définir les serveurs (local + production Render)
        List<Server> servers = new ArrayList<>();
        servers.add(new Server().url("http://localhost:8080").description("Local Development"));
        servers.add(new Server().url("https://gestion-compte-om-1.onrender.com").description("Production Render 1"));
        servers.add(new Server().url("https://gestion-compte-om-2.onrender.com").description("Production Render 2"));

        // ✅ Construire la documentation OpenAPI
        OpenAPI openAPI = new OpenAPI()
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

                // return the built OpenAPI
                // Also provide a customiser to ensure the security scheme is present on the final OpenAPI instance
                return openAPI;
        }

        }
