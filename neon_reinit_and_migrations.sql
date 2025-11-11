-- WARNING: This script will BACKUP public tables into schema backup_20251111_0015,
-- DROP and RECREATE the public schema, then apply the SQL migrations V3..V6.
-- Review before running. You can run it in Neon SQL editor or via psql.

BEGIN;

-- 1) create backup schema and copy tables
CREATE SCHEMA IF NOT EXISTS backup_20251111_0015;
DROP TABLE IF EXISTS backup_20251111_0015.utilisateurs;
DROP TABLE IF EXISTS backup_20251111_0015.comptes;
DROP TABLE IF EXISTS backup_20251111_0015.transactions;

CREATE TABLE backup_20251111_0015.utilisateurs AS TABLE public.utilisateurs;
CREATE TABLE backup_20251111_0015.comptes AS TABLE public.comptes;
CREATE TABLE backup_20251111_0015.transactions AS TABLE public.transactions;

-- 2) reinitialize public schema
DROP SCHEMA public CASCADE;
CREATE SCHEMA public;
GRANT ALL ON SCHEMA public TO public;

-- 3) apply migration V3 (create utilisateurs)
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

-- 4) apply migration V4 (create comptes)
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

-- 5) apply migration V5 (create transactions)
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

-- 6) apply migration V6 (add compte_id) -- adjusted to UUID to match comptes.id
ALTER TABLE transactions
    ADD COLUMN IF NOT EXISTS compte_id UUID;

ALTER TABLE transactions
    ADD CONSTRAINT IF NOT EXISTS fk_transactions_compte FOREIGN KEY (compte_id) REFERENCES comptes(id);

CREATE INDEX IF NOT EXISTS idx_transactions_compte_id ON transactions(compte_id);

-- 7) create a minimal flyway_schema_history table and insert markers for V3..V6
CREATE TABLE IF NOT EXISTS flyway_schema_history (
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
(1,'3','create_utilisateurs','SQL','V3__create_utilisateurs.sql','migration-agent',now(),true),
(2,'4','create_comptes','SQL','V4__create_comptes.sql','migration-agent',now(),true),
(3,'5','create_transactions','SQL','V5__create_transactions.sql','migration-agent',now(),true),
(4,'6','add_compte_to_transactions','SQL','V6__add_compte_to_transactions.sql','migration-agent',now(),true);

-- 8) sanity checks: list tables and counts
SELECT table_name FROM information_schema.tables WHERE table_schema = 'public' ORDER BY table_name;
SELECT 'utilisateurs' AS table, count(*) FROM public.utilisateurs;
SELECT 'comptes' AS table, count(*) FROM public.comptes;
SELECT 'transactions' AS table, count(*) FROM public.transactions;

COMMIT;
