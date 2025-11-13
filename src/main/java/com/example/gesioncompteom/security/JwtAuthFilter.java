package com.example.gesioncompteom.security;

import com.example.gesioncompteom.model.Utilisateur;
import com.example.gesioncompteom.repository.UtilisateurRepository;
import io.jsonwebtoken.*;
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
import java.util.Optional;
import java.util.UUID;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    @Value("${JWT_SECRET:default_jwt_secret_for_development_only}")
    private String jwtSecret;

    private final UtilisateurRepository utilisateurRepository;

    public JwtAuthFilter(UtilisateurRepository utilisateurRepository) {
        this.utilisateurRepository = utilisateurRepository;
    }

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

                    // Check if token expires within 5 minutes
                    long expirationTime = claims.getBody().getExpiration().getTime();
                    long currentTime = System.currentTimeMillis();
                    long timeToExpiry = expirationTime - currentTime;

                    String newToken = token;
                    if (timeToExpiry < 5 * 60 * 1000) { // Less than 5 minutes
                        // Auto-refresh the token
                        try {
                            Optional<Utilisateur> userOpt = utilisateurRepository.findById(UUID.fromString(subject));
                            if (userOpt.isPresent()) {
                                Utilisateur user = userOpt.get();
                                if ("ACTIF".equals(user.getStatut()) && user.getRefreshToken() != null) {
                                    // Generate new access token
                                    newToken = generateAccessToken(subject);
                                    // Set the new token in response header for client to use
                                    response.setHeader("X-New-Access-Token", newToken);
                                }
                            }
                        } catch (Exception refreshEx) {
                            // If refresh fails, continue with old token
                        }
                    }

                    // Determine authorities: if token has is_distributeur claim, grant ROLE_DISTRIBUTEUR
                    boolean isDistributeur = false;
                    Object isDist = claims.getBody().get("is_distributeur");
                    if (isDist instanceof Boolean) {
                        isDistributeur = (Boolean) isDist;
                    }

                    UsernamePasswordAuthenticationToken authentication;
                    if (isDistributeur) {
                        User principal = new User(subject, "", Collections.singletonList(new SimpleGrantedAuthority("ROLE_DISTRIBUTEUR")));
                        authentication = new UsernamePasswordAuthenticationToken(principal, newToken, principal.getAuthorities());
                    } else {
                        User principal = new User(subject, "", Collections.singletonList(new SimpleGrantedAuthority("ROLE_UTILISATEUR")));
                        authentication = new UsernamePasswordAuthenticationToken(principal, newToken, principal.getAuthorities());
                    }
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } catch (ExpiredJwtException ex) {
                    // Token is expired, try to refresh it
                    try {
                        Key key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
                        Claims claims = ex.getClaims();
                        String subject = claims.getSubject();

                        Optional<Utilisateur> userOpt = utilisateurRepository.findById(UUID.fromString(subject));
                        if (userOpt.isPresent()) {
                            Utilisateur user = userOpt.get();
                            if ("ACTIF".equals(user.getStatut()) && user.getRefreshToken() != null) {
                                // Generate new access token
                                String newToken = generateAccessToken(subject);
                                // Set the new token in response header
                                response.setHeader("X-New-Access-Token", newToken);

                                // Set authentication with new token
                                User principal = new User(subject, "", Collections.singletonList(new SimpleGrantedAuthority("ROLE_UTILISATEUR")));
                                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(principal, newToken, principal.getAuthorities());
                                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                                SecurityContextHolder.getContext().setAuthentication(authentication);
                            } else {
                                SecurityContextHolder.clearContext();
                            }
                        } else {
                            SecurityContextHolder.clearContext();
                        }
                    } catch (Exception refreshEx) {
                        SecurityContextHolder.clearContext();
                    }
                } catch (Exception ex) {
                    // invalid token -> clear context and continue (will be rejected by security)
                    SecurityContextHolder.clearContext();
                }
            }
        }

        filterChain.doFilter(request, response);
    }

    private String generateAccessToken(String userId) {
        Key key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        java.time.Instant now = java.time.Instant.now();
        return Jwts.builder()
                .setSubject(userId)
                .claim("is_verified", true)
                .setIssuedAt(java.util.Date.from(now))
                .setExpiration(java.util.Date.from(now.plus(15, java.time.temporal.ChronoUnit.MINUTES)))
                .signWith(key, io.jsonwebtoken.SignatureAlgorithm.HS256)
                .compact();
    }
}
