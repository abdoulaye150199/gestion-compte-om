package com.example.gesioncompteom.repository;

import com.example.gesioncompteom.model.Compte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CompteRepository extends JpaRepository<Compte, String> {
    Optional<Compte> findByNumeroCompte(String numeroCompte);
}

