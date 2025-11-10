package com.example.gesioncompteom.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.ArrayList;
import java.util.List;

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
            .contact(new Contact().name("Abdoulaye Diallo").email("abdoulaye@example.com"))
        )
        // Servers list: local dev and deployed (Render) placeholder.
        .servers(new ArrayList<>(List.of(
            new io.swagger.v3.oas.models.servers.Server().url("http://localhost:8080").description("Local development"),
            new io.swagger.v3.oas.models.servers.Server().url("https://your-app.onrender.com").description("Production (Render) - change to your real URL")
        )));
    }


    // If you prefer to hide specific paths from the OpenAPI spec programmatically,
    // you can provide an OpenApiCustomiser bean here. We avoid referencing
    // OpenApiCustomiser directly to keep compilation portable across springdoc
    // versions; instead prefer using @Hidden on controllers or configuring
    // springdoc properties.

    // No additional GroupedOpenApi bean required; default SpringDoc configuration will expose the API.
}

