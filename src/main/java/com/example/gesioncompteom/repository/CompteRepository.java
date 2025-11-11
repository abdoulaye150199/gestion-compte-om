package com.example.gesioncompteom.repository;

import com.example.gesioncompteom.model.Compte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CompteRepository extends JpaRepository<Compte, UUID> {
    Optional<Compte> findByNumeroCompte(String numeroCompte);
}

