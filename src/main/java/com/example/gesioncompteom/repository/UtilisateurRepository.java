package com.example.gesioncompteom.repository;

import com.example.gesioncompteom.model.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface UtilisateurRepository extends JpaRepository<Utilisateur, UUID> {
    Optional<Utilisateur> findByNumeroTelephone(String numeroTelephone);
}
