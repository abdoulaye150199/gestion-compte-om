package com.example.gesioncompteom.repository;

import com.example.gesioncompteom.model.Distributeur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface DistributeurRepository extends JpaRepository<Distributeur, UUID> {
    Optional<Distributeur> findByNumeroTelephone(String numeroTelephone);
    Optional<Distributeur> findByCodeDistributeur(String codeDistributeur);
}
