package com.example.gesioncompteom.repository;

import com.example.gesioncompteom.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
	org.springframework.data.domain.Page<com.example.gesioncompteom.model.Transaction> findByCompteId(UUID compteId, org.springframework.data.domain.Pageable pageable);
	List<Transaction> findByUtilisateurId(UUID utilisateurId);
}

