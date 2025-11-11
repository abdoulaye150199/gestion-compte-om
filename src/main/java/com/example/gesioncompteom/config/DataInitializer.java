package com.example.gesioncompteom.config;

import com.example.gesioncompteom.model.Compte;
import com.example.gesioncompteom.model.Utilisateur;
import com.example.gesioncompteom.repository.CompteRepository;
import com.example.gesioncompteom.repository.UtilisateurRepository;
import com.example.gesioncompteom.service.CompteService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.util.Optional;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initData(CompteService compteService, UtilisateurRepository utilisateurRepository, CompteRepository compteRepository) {
        return args -> {
            // create a vendor user if not exists (idempotent)
            Utilisateur vendeurUser = null;
            try {
                Optional<Utilisateur> existingVendeur = utilisateurRepository.findByNumeroTelephone("+221770000000");
                if (existingVendeur.isEmpty()) {
                    vendeurUser = new Utilisateur();
                    vendeurUser.setNom("Vendeur");
                    vendeurUser.setPrenom("Test");
                    vendeurUser.setNumeroTelephone("+221770000000");
                    vendeurUser.setVerified(true);
                    vendeurUser.setSolde(new BigDecimal("100000"));
                    utilisateurRepository.save(vendeurUser);
                    System.out.println("Utilisateur vendeur initialisé: " + vendeurUser.getNumeroTelephone());
                } else {
                    vendeurUser = existingVendeur.get();
                    System.out.println("Utilisateur vendeur déjà présent: " + vendeurUser.getNumeroTelephone());
                }
            } catch (Exception e) {
                System.out.println("Erreur lors de l'initialisation de l'utilisateur vendeur: " + e.getMessage());
            }

            // create a vendor account if not exists (idempotent)
            try {
                Compte vendeurCompte;
                try {
                    vendeurCompte = compteService.getByNumero("VENDEUR-001");
                    System.out.println("Vendeur déjà présent: VENDEUR-001");
                    // update utilisateurId if not set
                    if (vendeurCompte.getUtilisateurId() == null && vendeurUser != null) {
                        vendeurCompte.setUtilisateurId(vendeurUser.getId());
                        compteRepository.save(vendeurCompte);
                        System.out.println("Vendeur mis à jour avec utilisateurId");
                    }
                } catch (java.util.NoSuchElementException ex) {
                    vendeurCompte = Compte.builder()
                            .numeroCompte("VENDEUR-001")
                            .titulaire("Vendeur Test")
                            .solde(new BigDecimal("100000"))
                            .utilisateurId(vendeurUser != null ? vendeurUser.getId() : null)
                            .build();
                    compteService.create(vendeurCompte);
                    System.out.println("Vendeur initialisé: " + vendeurCompte.getNumeroCompte());
                }
            } catch (Exception e) {
                System.out.println("Erreur lors de l'initialisation du vendeur: " + e.getMessage());
            }
        };
    }
}

