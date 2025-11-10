package com.example.gesioncompteom.controller;

import com.example.gesioncompteom.model.Utilisateur;
import com.example.gesioncompteom.service.UtilisateurService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Map;

@RestController
@RequestMapping("/api/utilisateurs")
public class UtilisateurController {

    private final UtilisateurService service;

    public UtilisateurController(UtilisateurService service) {
        this.service = service;
    }

    record RegisterRequest(String nom, String prenom, String numeroTelephone) {}
    record VerifyRequest(String numeroTelephone, String codeVerification) {}

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest r) {
        Utilisateur u = service.register(r.nom(), r.prenom(), r.numeroTelephone());
        return ResponseEntity.created(URI.create("/api/utilisateurs/" + u.getId())).body(Map.of("id", u.getId(), "message", "Code envoyé"));
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verify(@RequestBody VerifyRequest v) {
        String token = service.verify(v.numeroTelephone(), v.codeVerification());
        return ResponseEntity.ok(Map.of("token", token));
    }

   
}
