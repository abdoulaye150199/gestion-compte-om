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
    private UUID id;

    @Column(name = "utilisateur_id", nullable = false)
    private UUID utilisateurId;

    @Column(name = "compte_id")
    private UUID compteId;

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
        if (id == null) id = UUID.randomUUID();
        if (dateTransaction == null) dateTransaction = OffsetDateTime.now();
    }
}

