package com.example.gesioncompteom.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class SwaggerViewConfig implements WebMvcConfigurer {
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // Forward the requested public path to the Springdoc Swagger UI index
        // Use forward so the browser URL remains /java/abdoulaye.diallo/docs
        registry.addViewController("/java/abdoulaye.diallo/docs").setViewName("forward:/swagger-ui/index.html");
        // Also accept a trailing slash
        registry.addViewController("/java/abdoulaye.diallo/docs/").setViewName("forward:/swagger-ui/index.html");
    }
}
