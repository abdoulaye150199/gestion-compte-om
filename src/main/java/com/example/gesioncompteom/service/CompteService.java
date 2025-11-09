package com.example.gesioncompteom.service;

import com.example.gesioncompteom.model.Compte;
import com.example.gesioncompteom.repository.CompteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@Transactional
public class CompteService {

    private final CompteRepository repo;

    public CompteService(CompteRepository repo) {
        this.repo = repo;
    }

    public Compte create(Compte c) {
        if (c.getSolde() == null) c.setSolde(BigDecimal.ZERO);
        return repo.save(c);
    }

    public Compte getById(String id) {
        return repo.findById(id).orElseThrow(() -> new NoSuchElementException("Compte not found"));
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
}

