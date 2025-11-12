package com.example.gesioncompteom.config;

import com.example.gesioncompteom.security.JwtAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final CorsConfigurationSource corsConfigurationSource;

    public SecurityConfig(CorsConfigurationSource corsConfigurationSource) {
        this.corsConfigurationSource = corsConfigurationSource;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthFilter jwtAuthFilter) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(auth -> auth
            // public endpoints
            .requestMatchers("/api/utilisateurs/register", "/api/utilisateurs/verify").permitAll()
            // distributeur login is public
            .requestMatchers("/api/distributeurs/login").permitAll()
            // allow OpenAPI and Swagger UI
            .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", "/java/**").permitAll()
            // allow actuator health if present
            .requestMatchers("/actuator/health", "/actuator/**").permitAll()
            // user and distributor endpoints
            .requestMatchers("/api/comptes/**", "/api/transactions/**").hasAnyAuthority("ROLE_UTILISATEUR", "ROLE_DISTRIBUTEUR")
            // distributor-only endpoints
            .requestMatchers("/api/distributeurs/depot", "/api/distributeurs/retrait").hasAuthority("ROLE_DISTRIBUTEUR")
            // everything else requires authentication
            .anyRequest().authenticated()
        )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
