package com.example.gesioncompteom.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "utilisateur_id", nullable = false)
    private String utilisateurId;

    @Column(name = "compte_id")
    private String compteId;

    @Column(nullable = false, precision = 38, scale = 2)
    private BigDecimal montant;

    @Column(name = "date_transaction")
    private OffsetDateTime dateTransaction;

    private String description;

    @Column(nullable = false)
    private String devise;

    @Column(nullable = false)
    private String statut;

    @Column(nullable = false)
    private String type;

    @PrePersist
    public void prePersist() {
        if (id == null) id = UUID.randomUUID().toString();
        if (dateTransaction == null) dateTransaction = OffsetDateTime.now();
    }
}

