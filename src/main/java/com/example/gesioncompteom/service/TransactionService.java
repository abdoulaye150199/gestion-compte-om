package com.example.gesioncompteom.service;

import com.example.gesioncompteom.model.Transaction;
import com.example.gesioncompteom.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@Transactional
public class TransactionService {

    private final TransactionRepository repo;

    public TransactionService(TransactionRepository repo) {
        this.repo = repo;
    }

    public Transaction create(Transaction t) {
        return repo.save(t);
    }

    public Transaction getById(String id) {
        try {
            UUID uuid = UUID.fromString(id);
            return repo.findById(uuid).orElseThrow(() -> new NoSuchElementException("Transaction not found"));
        } catch (IllegalArgumentException e) {
            throw new NoSuchElementException("Invalid Transaction ID format");
        }
    }

    public List<Transaction> listAll() { return repo.findAll(); }

    public List<Transaction> listByUtilisateurId(UUID utilisateurId) {
        return repo.findByUtilisateurId(utilisateurId);
    }
}

