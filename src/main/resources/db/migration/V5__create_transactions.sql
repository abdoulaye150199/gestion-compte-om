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
