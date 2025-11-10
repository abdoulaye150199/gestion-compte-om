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
