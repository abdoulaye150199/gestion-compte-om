package com.example.gesioncompteom.controller;

import com.example.gesioncompteom.model.Transaction;
import com.example.gesioncompteom.service.DistributeurService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/distributeurs")
public class DistributeurController {

    private final DistributeurService service;

    public DistributeurController(DistributeurService service) {
        this.service = service;
    }

    record LoginRequest(String numeroTelephone, String codeDistributeur) {}

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest r) {
        String key = r.numeroTelephone() != null && !r.numeroTelephone().isBlank() ? r.numeroTelephone() : r.codeDistributeur();
        if (key == null || key.isBlank()) return ResponseEntity.badRequest().body(Map.of("error", "numeroTelephone or codeDistributeur required"));
        try {
            var res = service.login(key);
            if (res.containsKey("ok") && res.get("ok").equals(Boolean.FALSE)) return ResponseEntity.status(401).body(Map.of("error", "invalid credentials"));
            return ResponseEntity.ok(res);
        } catch (Exception ex) {
            // log the exception server-side to help debugging and return a generic error to client
            ex.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "internal_error", "message", ex.getMessage()));
        }
    }

    record OperateRequest(String numeroClient, BigDecimal montant) {}

    @PostMapping("/depot")
    public ResponseEntity<?> deposit(@RequestBody OperateRequest r, Authentication auth) {
        // auth should contain distributor principal
        String distribId = auth.getName();
        Transaction t = service.depot(distribId, r.numeroClient(), r.montant());
        // fetch updated solde for the client
        var solde = service.getCompteSoldeForUserByPhone(r.numeroClient());
        return ResponseEntity.ok(Map.of("transactionId", t.getId(), "solde", solde));
    }

    @PostMapping("/retrait")
    public ResponseEntity<?> withdraw(@RequestBody OperateRequest r, Authentication auth) {
        String distribId = auth.getName();
        Transaction t = service.retrait(distribId, r.numeroClient(), r.montant());
        var solde = service.getCompteSoldeForUserByPhone(r.numeroClient());
        return ResponseEntity.ok(Map.of("transactionId", t.getId(), "solde", solde));
    }
}
