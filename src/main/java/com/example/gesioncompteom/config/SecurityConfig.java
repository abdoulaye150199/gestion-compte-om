package com.example.gesioncompteom.config;

import com.example.gesioncompteom.security.JwtAuthFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
public class SecurityConfig {

    @Autowired
    private CorsConfigurationSource corsConfigurationSource;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        JwtAuthFilter jwtFilter = new JwtAuthFilter();

        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                         // public endpoints
                         .requestMatchers("/api/utilisateurs/register", "/api/utilisateurs/verify").permitAll()
                         // allow OpenAPI and Swagger UI
                         .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", "/java/**").permitAll()
                         // allow actuator health if present
                         .requestMatchers("/actuator/health", "/actuator/**").permitAll()
                         // require authentication for compte and transaction endpoints
                         .requestMatchers("/api/comptes/**", "/api/transactions/**").authenticated()
                         // everything else permit all
                         .anyRequest().permitAll()
                 )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }
}
