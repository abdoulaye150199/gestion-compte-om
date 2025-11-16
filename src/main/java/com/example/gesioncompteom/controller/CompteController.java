package com.example.gesioncompteom.controller;

import com.example.gesioncompteom.model.Transaction;
import com.example.gesioncompteom.service.CompteService;
import com.example.gesioncompteom.service.UtilisateurService;
import com.example.gesioncompteom.util.QrUtil;
import com.example.gesioncompteom.model.Compte;
import com.example.gesioncompteom.assembler.CompteModelAssembler;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.util.Map;
import java.util.List;
import org.springframework.hateoas.EntityModel;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/comptes")
@SecurityRequirement(name = "bearerAuth")
public class CompteController {

    private final CompteService service;
    private final UtilisateurService utilisateurService;
    private final CompteModelAssembler assembler;

    public CompteController(CompteService service, UtilisateurService utilisateurService, CompteModelAssembler assembler) {
        this.service = service;
        this.utilisateurService = utilisateurService;
        this.assembler = assembler;
    }

    @GetMapping("/solde")
    @PreAuthorize("hasAnyAuthority('ROLE_UTILISATEUR', 'ROLE_DISTRIBUTEUR')")
    public ResponseEntity<?> getSolde() {
        String utilisateurId = extractUserIdFromToken();
        List<Compte> comptes = service.getAllByUtilisateurId(utilisateurId);
        Compte compte = comptes.stream().findFirst().orElse(null);
        BigDecimal solde = compte != null ? compte.getSolde() : BigDecimal.ZERO;
        return ResponseEntity.ok(Map.of("solde", solde));
    }


    record TransferRequest(String toUtilisateurTelephone, BigDecimal amount) {}

    @PostMapping("/transfert")
    @PreAuthorize("hasAuthority('ROLE_UTILISATEUR')")
    public ResponseEntity<?> transfert(@RequestBody TransferRequest r) {
        String utilisateurId = extractUserIdFromToken();
        Transaction t = service.transferByUtilisateurId(utilisateurId, r.toUtilisateurTelephone(), r.amount());

        // Get current user info for expediteur
        var currentUser = utilisateurService.findById(java.util.UUID.fromString(utilisateurId)).orElseThrow();

        // Get updated balance after transfer
        BigDecimal currentBalance = service.getSoldeByUtilisateurId(utilisateurId);

        // Extract destinataire from transaction description
        String destinataire = null;
        String desc = t.getDescription();
        if (desc != null && desc.contains("vers ")) {
            destinataire = desc.substring(desc.indexOf("vers ") + 5);
        }

        return ResponseEntity.ok(Map.of(
            "destinataire", destinataire,
            "expediteur", currentUser.getNom() + " " + currentUser.getPrenom(),
            "montant", t.getMontant().negate(), // Negative amount for outgoing transfer
            "date", t.getDateTransaction(),
            "reference", t.getId(),
            "solde", currentBalance
        ));
    }

    record PayRequest(String recipientIdentifier, BigDecimal amount) {}

    @PostMapping("/payer")
    @PreAuthorize("hasAuthority('ROLE_UTILISATEUR')")
    public ResponseEntity<?> payer(@RequestBody PayRequest r) {
        String utilisateurId = extractUserIdFromToken();
        Transaction t = service.payByUtilisateurId(utilisateurId, r.recipientIdentifier(), r.amount());
        return ResponseEntity.ok(Map.of("transactionId", t.getId()));
    }

    /**
     * Extrait l'ID utilisateur du token JWT
     */
    private String extractUserIdFromToken() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof User) {
            return ((User) principal).getUsername();
        }
        throw new IllegalStateException("Utilisateur non authentifié");
    }

    @GetMapping("/qr")
    @PreAuthorize("hasAnyAuthority('ROLE_UTILISATEUR', 'ROLE_DISTRIBUTEUR')")
    public ResponseEntity<?> qr() throws Exception {
        String utilisateurId = extractUserIdFromToken();
        List<Compte> comptes = service.getAllByUtilisateurId(utilisateurId);
        Compte c = comptes.stream().findFirst().orElse(null);
        if (c == null) {
            return ResponseEntity.notFound().build();
        }
        // QR contains the account numero
        String dataUrl = QrUtil.toDataUrlPng(c.getNumeroCompte(), 300);
        return ResponseEntity.ok(Map.of("qrDataUrl", dataUrl));
    }

    @GetMapping("")
    @PreAuthorize("hasAuthority('ROLE_UTILISATEUR')")
    public ResponseEntity<?> listComptes() {
        String utilisateurId = extractUserIdFromToken();
        List<Compte> comptes = service.getAllByUtilisateurId(utilisateurId);
    List<EntityModel<Compte>> models = comptes.stream().map(cpt -> assembler.toModel(cpt)).collect(Collectors.toList());
    return ResponseEntity.ok(models);
    }

    @PostMapping("")
    @PreAuthorize("hasAuthority('ROLE_UTILISATEUR')")
    public ResponseEntity<?> createCompte(@RequestBody CreateCompteRequest r) {
        String utilisateurId = extractUserIdFromToken();

        if (r == null || r.numeroCompte() == null || r.numeroCompte().isBlank() || r.codeSecret() == null || r.codeSecret().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "invalid_payload", "message", "numeroCompte and codeSecret are required"));
        }

        // Build Compte with minimal fields — the server will set defaults
        Compte c = new Compte();
        c.setNumeroCompte(r.numeroCompte());
        c.setCodeSecret(r.codeSecret());

        // set titulaire from utilisateur if available
        try {
            var opt = utilisateurService.findById(java.util.UUID.fromString(utilisateurId));
            if (opt.isPresent()) {
                var u = opt.get();
                c.setTitulaire(u.getNom() + " " + u.getPrenom());
            }
        } catch (Exception ex) {
            // ignore and let service set default titulaire
        }

        // ensure statut default will be applied by service.create
        Compte created = service.createForUtilisateur(utilisateurId, c);
        return ResponseEntity.created(URI.create("/api/comptes/" + created.getId())).body(created);
    }

    public record CreateCompteRequest(String numeroCompte, String codeSecret) {}

    @GetMapping("/dashboard")
    @PreAuthorize("hasAuthority('ROLE_UTILISATEUR')")
    public ResponseEntity<?> dashboard() throws Exception {
        String userIdStr = SecurityContextHolder.getContext().getAuthentication().getName();
        java.util.UUID userId = java.util.UUID.fromString(userIdStr);
        Map<String, Object> dashboard = utilisateurService.getDashboard(userId);
        return ResponseEntity.ok(dashboard);
    }
}
