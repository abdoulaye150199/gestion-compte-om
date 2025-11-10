CREATE TABLE comptes (
    id UUID PRIMARY KEY,
    date_creation TIMESTAMP NOT NULL DEFAULT now(),
    derniere_modification TIMESTAMP,
    version BIGINT,
    numero_compte VARCHAR(255) NOT NULL UNIQUE,
    solde NUMERIC(38,2) NOT NULL DEFAULT 0,
    statut VARCHAR(255) NOT NULL,
    titulaire VARCHAR(255) NOT NULL
);

ALTER TABLE comptes
    ADD CONSTRAINT comptes_statut_check CHECK (statut IN ('ACTIF','BLOQUE','FERME'));
