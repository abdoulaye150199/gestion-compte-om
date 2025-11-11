-- Safe migration script for Neon: without IF NOT EXISTS on ALTER TABLE
BEGIN;

-- Reinitialize
DROP SCHEMA IF EXISTS public CASCADE;
CREATE SCHEMA public;
GRANT ALL ON SCHEMA public TO public;

-- V3: create utilisateurs
CREATE TABLE utilisateurs (
    id UUID PRIMARY KEY,
    nom VARCHAR(255) NOT NULL,
    prenom VARCHAR(255) NOT NULL,
    numero_telephone VARCHAR(255) NOT NULL UNIQUE,
    code_verification VARCHAR(255),
    is_verified BOOLEAN NOT NULL DEFAULT FALSE,
    solde NUMERIC(38,2) NOT NULL DEFAULT 0,
    date_creation TIMESTAMP NOT NULL DEFAULT now()
);

-- V4: create comptes
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

-- V5: create transactions
CREATE TABLE transactions (
    id UUID PRIMARY KEY,
    montant NUMERIC(38,2) NOT NULL,
    date_transaction TIMESTAMP,
    utilisateur_id UUID NOT NULL,
    description VARCHAR(255),
    devise VARCHAR(255) NOT NULL,
    statut VARCHAR(255) NOT NULL,
    type VARCHAR(255) NOT NULL
);
ALTER TABLE transactions
    ADD CONSTRAINT transactions_statut_check CHECK (statut IN ('EN_ATTENTE','VALIDEE','ANNULEE'));
ALTER TABLE transactions
    ADD CONSTRAINT transactions_type_check CHECK (type IN ('DEPOT','RETRAIT','TRANSFERT','PAIEMENT'));
ALTER TABLE transactions
    ADD CONSTRAINT fk_transactions_utilisateur FOREIGN KEY (utilisateur_id) REFERENCES utilisateurs(id);

-- V6: add compte_id (UUID, matching comptes.id type)
ALTER TABLE transactions
    ADD COLUMN compte_id UUID;

ALTER TABLE transactions
    ADD CONSTRAINT fk_transactions_compte FOREIGN KEY (compte_id) REFERENCES comptes(id);

CREATE INDEX idx_transactions_compte_id ON transactions(compte_id);

-- Flyway history
CREATE TABLE flyway_schema_history (
    installed_rank INT PRIMARY KEY,
    version VARCHAR(50),
    description VARCHAR(200) NOT NULL,
    type VARCHAR(20) NOT NULL,
    script VARCHAR(1000) NOT NULL,
    installed_by VARCHAR(100) NOT NULL,
    installed_on TIMESTAMP WITH TIME ZONE DEFAULT now() NOT NULL,
    success BOOLEAN NOT NULL
);

INSERT INTO flyway_schema_history(installed_rank, version, description, type, script, installed_by, installed_on, success)
VALUES
(1,'3','create_utilisateurs','SQL','V3__create_utilisateurs.sql','migration',now(),true),
(2,'4','create_comptes','SQL','V4__create_comptes.sql','migration',now(),true),
(3,'5','create_transactions','SQL','V5__create_transactions.sql','migration',now(),true),
(4,'6','add_compte_to_transactions','SQL','V6__add_compte_to_transactions.sql','migration',now(),true);

-- Verify
SELECT 'Done! Tables:' as status;
SELECT table_name FROM information_schema.tables WHERE table_schema = 'public' ORDER BY table_name;

COMMIT;
