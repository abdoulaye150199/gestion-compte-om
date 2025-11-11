package com.example.gesioncompteom.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Collections;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    @Value("${JWT_SECRET:default_jwt_secret_for_development_only}")
    private String jwtSecret;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (jwtSecret != null && !jwtSecret.isBlank()) {
                try {
                    Key key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
                    Jws<Claims> claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
                    String subject = claims.getBody().getSubject();

                    // Determine authorities: if token has is_distributeur claim, grant ROLE_DISTRIBUTEUR
                    boolean isDistributeur = false;
                    Object isDist = claims.getBody().get("is_distributeur");
                    if (isDist instanceof Boolean) {
                        isDistributeur = (Boolean) isDist;
                    }

                    UsernamePasswordAuthenticationToken authentication;
                    if (isDistributeur) {
                        User principal = new User(subject, "", Collections.singletonList(new SimpleGrantedAuthority("ROLE_DISTRIBUTEUR")));
                        authentication = new UsernamePasswordAuthenticationToken(principal, token, principal.getAuthorities());
                    } else {
                        User principal = new User(subject, "", Collections.emptyList());
                        authentication = new UsernamePasswordAuthenticationToken(principal, token, Collections.emptyList());
                    }
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } catch (Exception ex) {
                    // invalid token -> clear context and continue (will be rejected by security)
                    SecurityContextHolder.clearContext();
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
