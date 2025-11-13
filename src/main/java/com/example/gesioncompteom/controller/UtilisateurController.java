package com.example.gesioncompteom.controller;

import com.example.gesioncompteom.model.Utilisateur;
import com.example.gesioncompteom.service.UtilisateurService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/utilisateurs")
public class UtilisateurController {

    private final UtilisateurService service;

    public UtilisateurController(UtilisateurService service) {
        this.service = service;
    }

    record RegisterRequest(String nom, String prenom, String numeroTelephone, String codeSecret) {}
    record VerifyRequest(String numeroTelephone, String codeSecret) {}
    record RefreshRequest(String refreshToken) {}

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest r) {
        Utilisateur u = service.register(r.nom(), r.prenom(), r.numeroTelephone(), r.codeSecret());
        return ResponseEntity.created(URI.create("/api/utilisateurs/" + u.getId())).body(Map.of(
            "message", "Utilisateur enregistré avec succès. Votre code OTP est : " + u.getOtp() + " (valide 5 min).",
            "utilisateur", Map.of(
                "id", u.getId(),
                "nom", u.getNom(),
                "prenom", u.getPrenom(),
                "numeroTelephone", u.getNumeroTelephone()
            ),
            "otp", u.getOtp()
        ));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody VerifyRequest v) {
        Map<String, String> tokens = service.login(v.numeroTelephone(), v.codeSecret());
        return ResponseEntity.ok(tokens);
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody RefreshRequest r) {
        String newAccessToken = service.refreshToken(r.refreshToken());
        return ResponseEntity.ok(Map.of("accessToken", newAccessToken));
    }

    @GetMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestParam String userId, @RequestParam String otp) {
        String message = service.verifyOtp(UUID.fromString(userId), otp);
        return ResponseEntity.ok(Map.of("message", message));
    }


}

