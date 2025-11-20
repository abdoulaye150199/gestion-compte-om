package com.example.gesioncompteom.config;

import com.example.gesioncompteom.model.Compte;
import com.example.gesioncompteom.model.Vendeur;
import com.example.gesioncompteom.model.Utilisateur;
import com.example.gesioncompteom.repository.CompteRepository;
import com.example.gesioncompteom.repository.VendeurRepository;
import com.example.gesioncompteom.repository.UtilisateurRepository;
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
    CommandLineRunner initData(CompteService compteService, VendeurRepository vendeurRepository, CompteRepository compteRepository, UtilisateurRepository utilisateurRepository) {
        return args -> {
            // Create test user if not exists
            try {
                Optional<Utilisateur> testUser = utilisateurRepository.findByNumeroTelephone("+221782917770");
                if (testUser.isEmpty()) {
                    Utilisateur u = new Utilisateur();
                    u.setId(java.util.UUID.fromString("254e66d1-f777-405f-89d3-c6a18cbaad1d"));
                    u.setNom("Test");
                    u.setPrenom("User");
                    u.setNumeroTelephone("+221782917770");
                    u.setCodeSecret("1599");
                    u.setStatut("ACTIF");
                    u.setVerified(true);
                    u.setSolde(java.math.BigDecimal.valueOf(50000));
                    utilisateurRepository.save(u);

                    // Create account for test user
                    Compte c = new Compte();
                    c.setUtilisateurId(u.getId());
                    c.setTitulaire(u.getNom() + " " + u.getPrenom());
                    c.setSolde(java.math.BigDecimal.valueOf(50000));
                    c.setStatut("ACTIF");
                    c.setNumeroCompte("ACC-TEST");
                    compteService.create(c);
                    System.out.println("Test user and account created.");
                } else {
                    // Ensure account exists for the user and has balance
                    List<Compte> comptes = compteRepository.findAllByUtilisateurId(testUser.get().getId());
                    if (comptes.isEmpty()) {
                        Compte c = new Compte();
                        c.setUtilisateurId(testUser.get().getId());
                        c.setTitulaire(testUser.get().getNom() + " " + testUser.get().getPrenom());
                        c.setSolde(java.math.BigDecimal.valueOf(50000));
                        c.setStatut("ACTIF");
                        c.setNumeroCompte("ACC-TEST");
                        compteService.create(c);
                        System.out.println("Account created for existing test user.");
                    } else {
                        // Update balance if zero
                        for (Compte c : comptes) {
                            if (c.getSolde().compareTo(java.math.BigDecimal.ZERO) == 0) {
                                c.setSolde(java.math.BigDecimal.valueOf(50000));
                                compteRepository.save(c);
                                System.out.println("Balance updated for existing test user account.");
                            }
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println("Error creating test user: " + e.getMessage());
            }

            // Create another test user for transfers
            try {
                Optional<Utilisateur> testUser2 = utilisateurRepository.findByNumeroTelephone("+221772345678");
                if (testUser2.isEmpty()) {
                    Utilisateur u2 = new Utilisateur();
                    u2.setId(java.util.UUID.fromString("44444444-5555-6666-7777-888888888888"));
                    u2.setNom("Test2");
                    u2.setPrenom("User2");
                    u2.setNumeroTelephone("+221772345678");
                    u2.setCodeSecret("1234");
                    u2.setStatut("ACTIF");
                    u2.setVerified(true);
                    u2.setSolde(java.math.BigDecimal.valueOf(1000));
                    utilisateurRepository.save(u2);

                    // Create account for test user 2
                    Compte c2 = new Compte();
                    c2.setUtilisateurId(u2.getId());
                    c2.setTitulaire(u2.getNom() + " " + u2.getPrenom());
                    c2.setSolde(java.math.BigDecimal.valueOf(1000));
                    c2.setStatut("ACTIF");
                    c2.setNumeroCompte("ACC-TEST2");
                    compteService.create(c2);
                    System.out.println("Test user 2 and account created.");
                }
            } catch (Exception e) {
                System.out.println("Error creating test user 2: " + e.getMessage());
            }

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

