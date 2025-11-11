package com.example.gesioncompteom.service;

import com.example.gesioncompteom.model.Distributeur;
import com.example.gesioncompteom.model.Transaction;
import com.example.gesioncompteom.model.Utilisateur;
import com.example.gesioncompteom.repository.DistributeurRepository;
import com.example.gesioncompteom.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;

@Service
public class DistributeurService {

    private final DistributeurRepository repo;
    private final UtilisateurService utilisateurService;
    private final CompteService compteService;
    private final TransactionRepository transactionRepository;

    public DistributeurService(DistributeurRepository repo, UtilisateurService utilisateurService, CompteService compteService, TransactionRepository transactionRepository) {
        this.repo = repo;
        this.utilisateurService = utilisateurService;
        this.compteService = compteService;
        this.transactionRepository = transactionRepository;
    }

    public Map<String, Object> login(String numeroOrCode) {
        // Try find by phone first
        Distributeur d = repo.findByNumeroTelephone(numeroOrCode)
                .orElseGet(() -> repo.findByCodeDistributeur(numeroOrCode).orElse(null));
        if (d == null) return Map.of("ok", false);

        String secret = System.getenv("JWT_SECRET");
        if (secret == null || secret.isBlank()) throw new IllegalStateException("JWT_SECRET not configured");
        Key key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        Instant now = Instant.now();
        String jwt = Jwts.builder()
                .setSubject(d.getId().toString())
                .claim("is_distributeur", true)
                .claim("distributeur_nom", d.getNom())
                .setIssuedAt(java.util.Date.from(now))
                .setExpiration(java.util.Date.from(now.plus(7, ChronoUnit.DAYS)))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        return Map.of(
                "token", jwt,
                "nom", d.getNom(),
                "prenom", d.getPrenom(),
                "date", now.toString()
        );
    }

    public Transaction depot(String distributeurId, String numeroClient, BigDecimal montant) {
        Utilisateur client = utilisateurService.findByNumeroFlexible(numeroClient).orElseThrow(() -> new IllegalArgumentException("Client not found"));
        // perform deposit on client's account
        Transaction t = compteService.depositByUtilisateurId(client.getId().toString(), montant);
        // annotate transaction with distributor info in description
        t.setDescription("Depot par distributeur: " + distributeurId);
        return transactionRepository.save(t);
    }

    public Transaction retrait(String distributeurId, String numeroClient, BigDecimal montant) {
        Utilisateur client = utilisateurService.findByNumeroFlexible(numeroClient).orElseThrow(() -> new IllegalArgumentException("Client not found"));
        // check and perform withdrawal
        Transaction t = compteService.withdrawByUtilisateurId(client.getId().toString(), montant);
        t.setDescription("Retrait par distributeur: " + distributeurId);
        return transactionRepository.save(t);
    }

    public java.math.BigDecimal getCompteSoldeForUserByPhone(String numeroClient) {
        Utilisateur client = utilisateurService.findByNumeroFlexible(numeroClient).orElseThrow(() -> new IllegalArgumentException("Client not found"));
        return compteService.getSoldeByUtilisateurId(client.getId().toString());
    }
}
