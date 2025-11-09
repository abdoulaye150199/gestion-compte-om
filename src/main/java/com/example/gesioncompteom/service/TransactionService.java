package com.example.gesioncompteom.service;

import com.example.gesioncompteom.model.Transaction;
import com.example.gesioncompteom.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

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
        return repo.findById(id).orElseThrow(() -> new NoSuchElementException("Transaction not found"));
    }

    public List<Transaction> listAll() { return repo.findAll(); }
}

