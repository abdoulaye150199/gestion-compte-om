package com.example.gesioncompteom.service;

import com.example.gesioncompteom.exception.NotFoundException;
import com.example.gesioncompteom.model.Compte;
import com.example.gesioncompteom.model.Transaction;
import com.example.gesioncompteom.model.Utilisateur;
import com.example.gesioncompteom.repository.CompteRepository;
import com.example.gesioncompteom.repository.TransactionRepository;
import com.example.gesioncompteom.repository.UtilisateurRepository;
import com.example.gesioncompteom.util.QrUtil;
import com.google.zxing.WriterException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.io.IOException;
import java.math.BigDecimal;

@Service
public class UtilisateurService {

    @Value("${JWT_SECRET:default_jwt_secret_for_development_only}")
    private String jwtSecret;

    private final UtilisateurRepository repo;
    private final SmsService smsService;
    private final CompteRepository compteRepository;
    private final TransactionRepository transactionRepository;

    public UtilisateurService(UtilisateurRepository repo, SmsService smsService, CompteRepository compteRepository, TransactionRepository transactionRepository) {
        this.repo = repo;
        this.smsService = smsService;
        this.compteRepository = compteRepository;
        this.transactionRepository = transactionRepository;
    }

    public Utilisateur register(String nom, String prenom, String numeroTelephone, String codeSecret) {
        Optional<Utilisateur> existing = findByNumeroFlexible(numeroTelephone);
        if (existing.isPresent()) {
            throw new IllegalArgumentException("Le numéro est déjà utilisé");
        }
        Utilisateur u = new Utilisateur();
        u.setNom(nom);
        u.setPrenom(prenom);
        u.setNumeroTelephone(cleanNumber(numeroTelephone));
        u.setCodeSecret(codeSecret);
        u.setStatut("INACTIF");
        String otp = String.format("%06d", new Random().nextInt(999999));
        u.setOtp(otp);
        u.setOtpExpiration(LocalDateTime.now().plusMinutes(5));
        repo.save(u);
        String link = "http://localhost:8080/api/utilisateur/verify-otp?userId=" + u.getId() + "&otp=" + otp;
        smsService.sendSms(u.getNumeroTelephone(), "Cliquez sur ce lien pour vérifier votre compte : " + link);
        return u;
    }

    public String verifyOtp(UUID userId, String otp) {
        Utilisateur u = repo.findById(userId).orElseThrow(() -> new NotFoundException("Utilisateur non trouvé"));
        if (u.getOtp() == null || !u.getOtp().equals(otp) || LocalDateTime.now().isAfter(u.getOtpExpiration())) {
            return "OTP invalide ou expiré.";
        }
        u.setStatut("ACTIF");
        u.setOtp(null);
        u.setOtpExpiration(null);
        repo.save(u);
        // Create account if not exists
        if (compteRepository.findByUtilisateurId(u.getId()).isEmpty()) {
            Compte c = new Compte();
            c.setUtilisateurId(u.getId());
            c.setTitulaire(u.getNom() + " " + u.getPrenom());
            c.setSolde(java.math.BigDecimal.ZERO);
            c.setStatut("ACTIF");
            c.setNumeroCompte("ACC-" + java.util.UUID.randomUUID().toString());
            compteRepository.save(c);
        }
        return "Votre compte OMPay est désormais actif.";
    }

    public Map<String, String> login(String numeroTelephone, String code) {
        Utilisateur u = findByNumeroFlexible(numeroTelephone).orElseThrow(() -> new NotFoundException("Utilisateur non trouvé"));
        if (u.getCodeSecret() == null || !u.getCodeSecret().equals(code)) {
            throw new IllegalArgumentException("Code invalide");
        }
        // Check if account is active
        if (!"ACTIF".equals(u.getStatut())) {
            throw new IllegalArgumentException("Compte non actif");
        }

        // Generate tokens
        String accessToken = generateAccessToken(u.getId().toString());
        String refreshToken = generateRefreshToken(u.getId().toString());

        // Store refresh token
        u.setRefreshToken(refreshToken);
        repo.save(u);

        return Map.of("accessToken", accessToken, "refreshToken", refreshToken);
    }

    private String generateAccessToken(String userId) {
        Key key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        Instant now = Instant.now();
        return Jwts.builder()
                .setSubject(userId)
                .claim("is_verified", true)
                .setIssuedAt(java.util.Date.from(now))
                .setExpiration(java.util.Date.from(now.plus(15, ChronoUnit.MINUTES))) // Short-lived access token
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    private String generateRefreshToken(String userId) {
        Key key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        Instant now = Instant.now();
        return Jwts.builder()
                .setSubject(userId)
                .setIssuedAt(java.util.Date.from(now))
                .setExpiration(java.util.Date.from(now.plus(7, ChronoUnit.DAYS))) // Long-lived refresh token
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String refreshToken(String refreshToken) {
        try {
            Key key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
            var claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(refreshToken);
            String userId = claims.getBody().getSubject();

            Utilisateur u = repo.findById(UUID.fromString(userId)).orElseThrow(() -> new NotFoundException("Utilisateur non trouvé"));

            // Verify the refresh token matches the stored one
            if (!refreshToken.equals(u.getRefreshToken())) {
                throw new IllegalArgumentException("Refresh token invalide");
            }

            // Check if account is still active
            if (!"ACTIF".equals(u.getStatut())) {
                throw new IllegalArgumentException("Compte non actif");
            }

            return generateAccessToken(userId);
        } catch (Exception e) {
            throw new IllegalArgumentException("Refresh token invalide");
        }
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

    public Optional<Utilisateur> findById(UUID userId) {
        return repo.findById(userId);
    }

    public Map<String, Object> getDashboard(UUID userId) throws WriterException, IOException {
        Utilisateur u = repo.findById(userId).orElseThrow(() -> new NotFoundException("Utilisateur non trouvé"));
        Compte compte = compteRepository.findByUtilisateurId(userId).stream().findFirst().orElse(null);

        BigDecimal solde = compte != null ? compte.getSolde() : BigDecimal.ZERO;
        String qrCode = compte != null ? QrUtil.toDataUrlPng(compte.getNumeroCompte(), 200) : null;

        List<Transaction> allTransactions = transactionRepository.findByUtilisateurId(userId);
        // Get last 5 transactions, sorted by date descending
        List<Map<String, Object>> lastTransactions = allTransactions.stream()
                .sorted((t1, t2) -> t2.getDateTransaction().compareTo(t1.getDateTransaction()))
                .limit(5)
                .map(t -> {
                    String destinataire = null;
                    String expediteur = u.getNom() + " " + u.getPrenom(); // Current user is sender

                    // Extract recipient from description based on type
                    if ("TRANSFERT".equals(t.getType()) || "PAIEMENT".equals(t.getType())) {
                        // Description contains recipient info like "Transfert vers ACC-..." or "Paiement à ..."
                        String desc = t.getDescription();
                        if (desc != null) {
                            if (desc.contains("vers ")) {
                                destinataire = desc.substring(desc.indexOf("vers ") + 5);
                            } else if (desc.contains("à ")) {
                                destinataire = desc.substring(desc.indexOf("à ") + 2);
                            }
                        }
                    }

                    return Map.<String, Object>of(
                            "montant", t.getMontant(),
                            "description", t.getDescription(),
                            "dateTransaction", t.getDateTransaction(),
                            "type", t.getType(),
                            "statut", t.getStatut(),
                            "destinataire", destinataire,
                            "expediteur", expediteur,
                            "date", t.getDateTransaction(),
                            "reference", t.getId()
                    );
                })
                .toList();

        return Map.of(
                "nom", u.getNom(),
                "prenom", u.getPrenom(),
                "qrCode", qrCode,
                "solde", solde,
                "lastTransactions", lastTransactions
        );
    }
}
