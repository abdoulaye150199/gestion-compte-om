package com.example.gesioncompteom.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "comptes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Compte {

    @Id
    @Column(length = 36)
    private String id;

    @Column(unique = true, nullable = false)
    private String numeroCompte;

    @Column(nullable = false)
    private String titulaire;

    @Column(nullable = false)
    private BigDecimal solde;

    private LocalDateTime dateCreation;

    private String statut;

    @PrePersist
    public void prePersist() {
        if (id == null) id = UUID.randomUUID().toString();
        if (dateCreation == null) dateCreation = LocalDateTime.now();
        if (solde == null) solde = BigDecimal.ZERO;
        if (statut == null) statut = "ACTIVE";
    }
}

