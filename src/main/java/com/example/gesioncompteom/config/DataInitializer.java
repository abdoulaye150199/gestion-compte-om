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
                // Check if vendor account already exists
                compteService.getByNumero("VENDEUR-001");
                System.out.println("Vendeur déjà présent: VENDEUR-001");
            } catch (Exception e) {
                // If not found, create it
                Compte vendeur = Compte.builder()
                        .numeroCompte("VENDEUR-001")
                        .titulaire("Vendeur Test")
                        .solde(new BigDecimal("100000"))
                        .build();
                compteService.create(vendeur);
                System.out.println("Vendeur initialisé: " + vendeur.getNumeroCompte());
            }
        };
    }
}

