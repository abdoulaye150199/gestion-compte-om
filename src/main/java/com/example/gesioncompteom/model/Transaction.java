package com.example.gesioncompteom.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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

    @Column(nullable = false)
    private String compteDebiteurId;

    @Column(nullable = false)
    private String compteCrediteurId;

    @Column(nullable = false)
    private BigDecimal montant;

    private String motif;

    private LocalDateTime dateCreation;

    private String statut;

    @PrePersist
    public void prePersist() {
        if (id == null) id = UUID.randomUUID().toString();
        if (dateCreation == null) dateCreation = LocalDateTime.now();
        if (statut == null) statut = "PENDING";
    }
}

