package com.example.gesioncompteom.controller;

import com.example.gesioncompteom.model.Transaction;
import com.example.gesioncompteom.service.CompteService;
import com.example.gesioncompteom.util.QrUtil;
import com.example.gesioncompteom.model.Compte;
import com.example.gesioncompteom.assembler.CompteModelAssembler;
import org.springframework.http.ResponseEntity;
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

    @GetMapping("/{numero}/solde")
    public ResponseEntity<?> getSolde(@PathVariable("numero") String numero) {
        BigDecimal solde = service.getSoldeByNumero(numero);
        return ResponseEntity.ok(Map.of("numeroCompte", numero, "solde", solde));
    }

    

    record AmountRequest(String utilisateurId, BigDecimal amount) {}

    @PostMapping("/{numero}/depot")
    public ResponseEntity<?> depot(@PathVariable("numero") String numero, @RequestBody AmountRequest r) {
        Transaction t = service.depositByNumero(numero, r.amount(), r.utilisateurId());
        return ResponseEntity.ok(Map.of("transactionId", t.getId()));
    }

    @PostMapping("/{numero}/retrait")
    public ResponseEntity<?> retrait(@PathVariable("numero") String numero, @RequestBody AmountRequest r) {
        Transaction t = service.withdrawByNumero(numero, r.amount(), r.utilisateurId());
        return ResponseEntity.ok(Map.of("transactionId", t.getId()));
    }

    record TransferRequest(String utilisateurId, String toNumero, BigDecimal amount) {}

    @PostMapping("/{numero}/transfert")
    public ResponseEntity<?> transfert(@PathVariable("numero") String numero, @RequestBody TransferRequest r) {
        Transaction t = service.transferByNumero(numero, r.toNumero(), r.amount(), r.utilisateurId());
        return ResponseEntity.ok(Map.of("transactionId", t.getId()));
    }

    record PayRequest(String utilisateurId, String merchantNumero, BigDecimal amount) {}

    @PostMapping("/{numero}/payer")
    public ResponseEntity<?> payer(@PathVariable("numero") String numero, @RequestBody PayRequest r) {
        Transaction t = service.transferByNumero(numero, r.merchantNumero(), r.amount(), r.utilisateurId());
        return ResponseEntity.ok(Map.of("transactionId", t.getId()));
    }

    

    @GetMapping("/{numero}/qr")
    public ResponseEntity<?> qr(@PathVariable("numero") String numero) throws Exception {
        // QR contains the account identifier (numeroCompte)
        String dataUrl = QrUtil.toDataUrlPng(numero, 300);
        return ResponseEntity.ok(Map.of("qrDataUrl", dataUrl));
    }
}
