package com.example.gesioncompteom.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "vendeurs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vendeur {

    @Id
    private UUID id;

    private String nom;
    private String prenom;

    @Column(name = "numero_telephone", unique = true)
    private String numeroTelephone;

    @Column(name = "code_marchant", unique = true)
    private String codeMarchant;

    @Column(name = "date_creation")
    private OffsetDateTime dateCreation;

    @PrePersist
    public void prePersist() {
        if (id == null) id = UUID.randomUUID();
        if (dateCreation == null) dateCreation = OffsetDateTime.now();
    }
}