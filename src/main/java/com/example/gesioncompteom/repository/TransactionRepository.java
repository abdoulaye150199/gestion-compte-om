package com.example.gesioncompteom.repository;

import com.example.gesioncompteom.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, String> {
	org.springframework.data.domain.Page<com.example.gesioncompteom.model.Transaction> findByCompteId(String compteId, org.springframework.data.domain.Pageable pageable);
}

