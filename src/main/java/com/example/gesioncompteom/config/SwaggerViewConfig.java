package com.example.gesioncompteom.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class SwaggerViewConfig implements WebMvcConfigurer {
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // redirect /swagger-ui and /swagger-ui.html to the springdoc path
        registry.addViewController("/swagger-ui").setViewName("redirect:/swagger-ui/index.html");
        registry.addViewController("/swagger-ui.html").setViewName("redirect:/swagger-ui/index.html");
    }
}

