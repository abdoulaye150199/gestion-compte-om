package com.example.gesioncompteom.service;

import com.example.gesioncompteom.exception.NotFoundException;
import com.example.gesioncompteom.model.Utilisateur;
import com.example.gesioncompteom.model.Compte;
import com.example.gesioncompteom.repository.UtilisateurRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.List;
import java.util.ArrayList;
import java.math.BigDecimal;

@Service
public class UtilisateurService {

    private final UtilisateurRepository repo;
    private final SmsService smsService;
    private final CompteService compteService;

    @Value("${JWT_SECRET}")
    private String jwtSecret;

    public UtilisateurService(UtilisateurRepository repo, SmsService smsService, CompteService compteService) {
        this.repo = repo;
        this.smsService = smsService;
        this.compteService = compteService;
    }

    public Utilisateur register(String nom, String prenom, String numeroTelephone, String codeVerification) {
        Optional<Utilisateur> existing = findByNumeroFlexible(numeroTelephone);
        if (existing.isPresent()) {
            throw new IllegalArgumentException("Le numéro est déjà utilisé");
        }
        Utilisateur u = new Utilisateur();
        u.setNom(nom);
        u.setPrenom(prenom);
        u.setNumeroTelephone(cleanNumber(numeroTelephone));
        String code = (codeVerification != null && !codeVerification.isBlank()) ? codeVerification : generate4DigitCode();
        u.setCodeVerification(code);
        u.setVerified(false);
        repo.save(u);
        smsService.sendSms(numeroTelephone, "Votre code de vérification : " + code);
        return u;
    }

    private String generate4DigitCode() {
        Random r = new Random();
        int n = 1000 + r.nextInt(9000);
        return String.valueOf(n);
    }

    public String verify(String numeroTelephone, String code) {
        Utilisateur u = findByNumeroFlexible(numeroTelephone).orElseThrow(() -> new NotFoundException("Utilisateur non trouvé"));
        if (u.getCodeVerification() == null || !u.getCodeVerification().equals(code)) {
            throw new IllegalArgumentException("Code invalide");
        }
        // Marquer comme vérifié si ne l'est pas déjà
        if (!u.isVerified()) {
            u.setVerified(true);
            repo.save(u);
            
            // Create account for user if they don't have one
            try {
                compteService.getByUtilisateurIdDirect(u.getId().toString());
            } catch (Exception e) {
                // Account doesn't exist, create it
                Compte compte = Compte.builder()
                        .numeroCompte("ACC-" + UUID.randomUUID().toString())
                        .solde(BigDecimal.ZERO)
                        .statut("ACTIF")
                        .titulaire(u.getNom() + " " + u.getPrenom())
                        .utilisateurId(u.getId())
                        .build();
                compteService.create(compte);
            }
        }

        if (jwtSecret == null || jwtSecret.isBlank()) {
            throw new IllegalStateException("JWT_SECRET n'est pas configuré");
        }
        Key key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        Instant now = Instant.now();
        String jwt = Jwts.builder()
                .setSubject(u.getId().toString())
                .claim("is_verified", true)
                .setIssuedAt(java.util.Date.from(now))
                .setExpiration(java.util.Date.from(now.plus(7, ChronoUnit.DAYS)))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        return jwt;
    }

    /**
     * Retourne un Optional<Utilisateur> en essayant plusieurs variantes du numéro
     * (raw, avec +, avec 0 prefix, avec +221) — utile pour supporter différents formats
     * envoyés par le client.
     */
    public Optional<Utilisateur> findByNumeroFlexible(String numero) {
        String cleaned = cleanNumber(numero);
        List<String> candidates = new ArrayList<>();
        candidates.add(cleaned);
        if (cleaned.startsWith("+")) {
            candidates.add(cleaned.substring(1));
        } else {
            candidates.add("+" + cleaned);
        }
        if (!cleaned.startsWith("0")) {
            candidates.add("0" + cleaned);
        }
        // Add common country prefix for West Africa (Senegal) if number looks like local 9-digit starting with 7
        if (!cleaned.startsWith("+221") && cleaned.length() == 9) {
            candidates.add("+221" + cleaned);
            candidates.add("221" + cleaned);
        }

        for (String c : candidates) {
            Optional<Utilisateur> u = repo.findByNumeroTelephone(c);
            if (u.isPresent()) return u;
        }
        return Optional.empty();
    }

    private String cleanNumber(String numero) {
        if (numero == null) return null;
        String n = numero.replaceAll("[^+0-9]", "");
        // normalize leading 00 to +
        if (n.startsWith("00")) {
            n = "+" + n.substring(2);
        }
        return n;
    }
}
