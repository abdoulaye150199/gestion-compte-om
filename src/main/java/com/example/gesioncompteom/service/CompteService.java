package com.example.gesioncompteom.service;

import com.example.gesioncompteom.model.Compte;
import com.example.gesioncompteom.repository.CompteRepository;
import com.example.gesioncompteom.repository.UtilisateurRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import com.example.gesioncompteom.model.Transaction;
import com.example.gesioncompteom.model.Utilisateur;
import com.example.gesioncompteom.repository.TransactionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

@Service
@Transactional
public class CompteService {

    private final CompteRepository repo;
    private final TransactionRepository transactionRepository;
    private final UtilisateurRepository utilisateurRepository;

    public CompteService(CompteRepository repo, TransactionRepository transactionRepository, UtilisateurRepository utilisateurRepository) {
        this.repo = repo;
        this.transactionRepository = transactionRepository;
        this.utilisateurRepository = utilisateurRepository;
    }

    public Compte create(Compte c) {
        if (c.getSolde() == null) c.setSolde(BigDecimal.ZERO);
        if (c.getStatut() == null) c.setStatut("ACTIF");
        if (c.getNumeroCompte() == null) c.setNumeroCompte("ACC-" + java.util.UUID.randomUUID().toString());
        if (c.getTitulaire() == null) c.setTitulaire("UNKNOWN");
        return repo.save(c);
    }

    public BigDecimal getSoldeByNumero(String numeroCompte) {
        Compte c = getByNumero(numeroCompte);
        return c.getSolde();
    }

    public Transaction depositByNumero(String numeroCompte, BigDecimal amount, String utilisateurId) {
        Compte c = getByNumero(numeroCompte);
        c.setSolde(c.getSolde().add(amount));
        repo.save(c);
        Transaction t = Transaction.builder()
                .compteId(c.getId())
                .utilisateurId(UUID.fromString(utilisateurId))
                .montant(amount)
                .devise("XOF")
                .statut("VALIDEE")
                .type("DEPOT")
                .description("Depot via API")
                .build();
        return transactionRepository.save(t);
    }

    public Transaction withdrawByNumero(String numeroCompte, BigDecimal amount, String utilisateurId) {
        Compte c = getByNumero(numeroCompte);
        if (c.getSolde().compareTo(amount) < 0) throw new IllegalArgumentException("insufficient_balance");
        c.setSolde(c.getSolde().subtract(amount));
        repo.save(c);
        Transaction t = Transaction.builder()
                .compteId(c.getId())
                .utilisateurId(UUID.fromString(utilisateurId))
                .montant(amount)
                .devise("XOF")
                .statut("VALIDEE")
                .type("RETRAIT")
                .description("Retrait via API")
                .build();
        return transactionRepository.save(t);
    }

    @org.springframework.transaction.annotation.Transactional
    public Transaction transferByNumero(String fromNumero, String toNumero, BigDecimal amount, String utilisateurId) {
        Compte from = getByNumero(fromNumero);
        Compte to = getByNumero(toNumero);
        if (from.getSolde().compareTo(amount) < 0) throw new IllegalArgumentException("insufficient_balance");
        from.setSolde(from.getSolde().subtract(amount));
        to.setSolde(to.getSolde().add(amount));
        repo.save(from);
        repo.save(to);

        Transaction t = Transaction.builder()
                .compteId(from.getId())
                .utilisateurId(UUID.fromString(utilisateurId))
                .montant(amount)
                .devise("XOF")
                .statut("VALIDEE")
                .type("TRANSFERT")
                .description("Transfert vers " + to.getNumeroCompte())
                .build();
        return transactionRepository.save(t);
    }

    public Page<Transaction> listTransactionsByNumero(String numeroCompte, int page, int size, String sortBy, String direction) {
        Compte c = getByNumero(numeroCompte);
        Sort sort = Sort.by(Sort.Direction.fromString(direction), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        return transactionRepository.findByCompteId(c.getId(), pageable);
    }

    public Compte getById(String id) {
        try {
            UUID uuid = UUID.fromString(id);
            return repo.findById(uuid).orElseThrow(() -> new NoSuchElementException("Compte not found"));
        } catch (IllegalArgumentException e) {
            throw new NoSuchElementException("Invalid Compte ID format");
        }
    }

    public Compte getByNumero(String numero) {
        return repo.findByNumeroCompte(numero).orElseThrow(() -> new NoSuchElementException("Compte not found"));
    }

    public List<Compte> listAll() { return repo.findAll(); }

    public Compte credit(String id, BigDecimal amount) {
        Compte c = getById(id);
        c.setSolde(c.getSolde().add(amount));
        return repo.save(c);
    }

    public Compte debit(String id, BigDecimal amount) {
        Compte c = getById(id);
        if (c.getSolde().compareTo(amount) < 0) throw new IllegalArgumentException("insufficient_balance");
        c.setSolde(c.getSolde().subtract(amount));
        return repo.save(c);
    }

    // ===== Methods based on utilisateurId (for token-based API) =====

    private Compte getByUtilisateurId(String utilisateurId) {
        UUID uuid = UUID.fromString(utilisateurId);
        return repo.findByUtilisateurId(uuid).orElseThrow(() -> new NoSuchElementException("Compte not found for user"));
    }

    public BigDecimal getSoldeByUtilisateurId(String utilisateurId) {
        Compte c = getByUtilisateurId(utilisateurId);
        return c.getSolde();
    }

    public Transaction depositByUtilisateurId(String utilisateurId, BigDecimal amount) {
        Compte c = getByUtilisateurId(utilisateurId);
        c.setSolde(c.getSolde().add(amount));
        repo.save(c);
        Transaction t = Transaction.builder()
                .compteId(c.getId())
                .utilisateurId(UUID.fromString(utilisateurId))
                .montant(amount)
                .devise("XOF")
                .statut("VALIDEE")
                .type("DEPOT")
                .description("Depot via API")
                .build();
        return transactionRepository.save(t);
    }

    public Transaction withdrawByUtilisateurId(String utilisateurId, BigDecimal amount) {
        Compte c = getByUtilisateurId(utilisateurId);
        if (c.getSolde().compareTo(amount) < 0) throw new IllegalArgumentException("insufficient_balance");
        c.setSolde(c.getSolde().subtract(amount));
        repo.save(c);
        Transaction t = Transaction.builder()
                .compteId(c.getId())
                .utilisateurId(UUID.fromString(utilisateurId))
                .montant(amount)
                .devise("XOF")
                .statut("VALIDEE")
                .type("RETRAIT")
                .description("Retrait via API")
                .build();
        return transactionRepository.save(t);
    }

    @org.springframework.transaction.annotation.Transactional
    public Transaction transferByUtilisateurId(String fromUtilisateurId, String toUtilisateurTelephone, BigDecimal amount) {
        Compte from = getByUtilisateurId(fromUtilisateurId);
        // Find recipient by telephone number (from Utilisateur table)
        // For simplicity, we'll find the account linked to that user
        // This requires looking up the user by phone, then their account
        Compte to = getByUtilisateurPhoneNumber(toUtilisateurTelephone);
        
        if (from.getSolde().compareTo(amount) < 0) throw new IllegalArgumentException("insufficient_balance");
        from.setSolde(from.getSolde().subtract(amount));
        to.setSolde(to.getSolde().add(amount));
        repo.save(from);
        repo.save(to);

        Transaction t = Transaction.builder()
                .compteId(from.getId())
                .utilisateurId(UUID.fromString(fromUtilisateurId))
                .montant(amount)
                .devise("XOF")
                .statut("VALIDEE")
                .type("TRANSFERT")
                .description("Transfert vers " + to.getNumeroCompte())
                .build();
        return transactionRepository.save(t);
    }

    private Compte getByUtilisateurPhoneNumber(String phoneNumber) {
        // Look up user by phone number
        Utilisateur u = utilisateurRepository.findByNumeroTelephone(phoneNumber)
                .orElseThrow(() -> new NoSuchElementException("User not found with phone: " + phoneNumber));
        // Get account for that user
        return repo.findByUtilisateurId(u.getId())
                .orElseThrow(() -> new NoSuchElementException("Account not found for user"));
    }
}

