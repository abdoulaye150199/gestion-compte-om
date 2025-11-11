package com.example.gesioncompteom.controller;

import com.example.gesioncompteom.model.Transaction;
import com.example.gesioncompteom.service.CompteService;
import com.example.gesioncompteom.util.QrUtil;
import com.example.gesioncompteom.model.Compte;
import com.example.gesioncompteom.assembler.CompteModelAssembler;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/comptes")
public class CompteController {

    private final CompteService service;
    private final CompteModelAssembler assembler;

    public CompteController(CompteService service, CompteModelAssembler assembler) {
        this.service = service;
        this.assembler = assembler;
    }

    @GetMapping("/solde")
    public ResponseEntity<?> getSolde() {
        String utilisateurId = extractUserIdFromToken();
        BigDecimal solde = service.getSoldeByUtilisateurId(utilisateurId);
        return ResponseEntity.ok(Map.of("solde", solde));
    }


    record TransferRequest(String toUtilisateurTelephone, BigDecimal amount) {}

    @PostMapping("/transfert")
    public ResponseEntity<?> transfert(@RequestBody TransferRequest r) {
        String utilisateurId = extractUserIdFromToken();
        Transaction t = service.transferByUtilisateurId(utilisateurId, r.toUtilisateurTelephone(), r.amount());
        return ResponseEntity.ok(Map.of("transactionId", t.getId()));
    }

    record PayRequest(String merchantTelephone, BigDecimal amount) {}

    @PostMapping("/payer")
    public ResponseEntity<?> payer(@RequestBody PayRequest r) {
        String utilisateurId = extractUserIdFromToken();
        Transaction t = service.payByUtilisateurId(utilisateurId, r.merchantTelephone(), r.amount());
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
    public ResponseEntity<?> qr() throws Exception {
        String utilisateurId = extractUserIdFromToken();
        Compte c = service.getByUtilisateurIdDirect(utilisateurId);
        // QR contains the account numero
        String dataUrl = QrUtil.toDataUrlPng(c.getNumeroCompte(), 300);
        return ResponseEntity.ok(Map.of("qrDataUrl", dataUrl));
    }
}
