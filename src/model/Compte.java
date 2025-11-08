package com.example.gestioncompteom.model;
import java.io.Serializable;
@Data
@Entity
Public class Compte {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String numeroCompte;
    private String titulaire;
    private double solde;
    private LocalDate dateCreation;
    private String Statut;

    @Embedded
    private Metadata metadata= new Metadata();

    @Embiddable
    @Data
    public static class Metadata {
        private LocalDateTime derniereModification;

        private int version;
    }
}