package com.example.gesioncompteom.config;

import com.example.gesioncompteom.model.Compte;
import com.example.gesioncompteom.service.CompteService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initData(CompteService compteService) {
        return args -> {
            // create a vendor account if not exists
            try {
                Compte vendeur = Compte.builder()
                        .numeroCompte("VENDEUR-001")
                        .titulaire("Vendeur Test")
                        .solde(new BigDecimal("100000"))
                        .build();
                compteService.create(vendeur);
                System.out.println("Vendeur initialisé: " + vendeur.getNumeroCompte());
            } catch (Exception e) {
                System.out.println("Vendeur déjà présent ou erreur: " + e.getMessage());
            }
        };
    }
}

