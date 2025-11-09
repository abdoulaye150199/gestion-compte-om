package com.example.gesioncompteom.config;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

@Configuration
public class MessageConfig {

    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        // message.yaml is YAML, Spring's MessageSource expects properties; Spring Boot automatically
        // loads messages*.properties, but we have YAML. To keep simple, we also provide messages.properties fallback.
        // Use basename 'classpath:message' to load message.yaml if supported, otherwise ensure messages.properties exists.
        messageSource.setBasename("classpath:message");
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setCacheSeconds(3600);
        return messageSource;
    }
}

