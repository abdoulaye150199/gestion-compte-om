package com.example.gesioncompteom.config;

import com.example.gesioncompteom.model.Compte;
import com.example.gesioncompteom.model.Vendeur;
import com.example.gesioncompteom.repository.CompteRepository;
import com.example.gesioncompteom.repository.VendeurRepository;
import com.example.gesioncompteom.service.CompteService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initData(CompteService compteService, VendeurRepository vendeurRepository, CompteRepository compteRepository) {
        return args -> {
            // Create accounts for seeded vendeurs
            try {
                List<Vendeur> vendeurs = vendeurRepository.findAll();
                for (Vendeur vendeur : vendeurs) {
                    try {
                        // Check if account already exists
                        compteService.getByNumero(vendeur.getCodeMarchant());
                        System.out.println("Account already exists for vendeur: " + vendeur.getCodeMarchant());
                    } catch (java.util.NoSuchElementException ex) {
                        // Create account for vendeur
                        Compte vendeurCompte = Compte.builder()
                                .numeroCompte(vendeur.getCodeMarchant())
                                .titulaire(vendeur.getNom() + " " + vendeur.getPrenom())
                                .solde(BigDecimal.ZERO) // Start with zero balance
                                .utilisateurId(null) // Vendeurs don't have utilisateur_id
                                .statut("ACTIF")
                                .build();
                        compteService.create(vendeurCompte);
                        System.out.println("Account created for vendeur: " + vendeur.getCodeMarchant());
                    }
                }
            } catch (Exception e) {
                System.out.println("Error initializing vendeur accounts: " + e.getMessage());
                e.printStackTrace();
            }
        };
    }
}

