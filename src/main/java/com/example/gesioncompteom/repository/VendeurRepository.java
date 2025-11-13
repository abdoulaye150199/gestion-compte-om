package com.example.gesioncompteom.repository;

import com.example.gesioncompteom.model.Vendeur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface VendeurRepository extends JpaRepository<Vendeur, UUID> {
    Optional<Vendeur> findByNumeroTelephone(String numeroTelephone);
    Optional<Vendeur> findByCodeMarchant(String codeMarchant);
}