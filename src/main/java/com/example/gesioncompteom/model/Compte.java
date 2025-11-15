package com.example.gesioncompteom.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "comptes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Compte {

    @Id
    private UUID id;

    @Column(name = "date_creation")
    private OffsetDateTime dateCreation;

    @Column(name = "derniere_modification")
    private OffsetDateTime derniereModification;

    @Version
    private Long version;

    @Column(name = "numero_compte", nullable = false, unique = true)
    private String numeroCompte;

    @Column(nullable = false, precision = 38, scale = 2)
    private BigDecimal solde;

    @Column(nullable = false)
    private String statut;

    @Column(nullable = false)
    private String titulaire;

    @Column(name = "code_secret")
    private String codeSecret;

    @Column(name = "utilisateur_id")
    private UUID utilisateurId;

    @PrePersist
    public void prePersist() {
        if (id == null) id = UUID.randomUUID();
        if (dateCreation == null) dateCreation = OffsetDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        derniereModification = OffsetDateTime.now();
    }
}
