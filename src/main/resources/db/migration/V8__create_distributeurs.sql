-- Create table for distributeurs (idempotent)
CREATE TABLE IF NOT EXISTS distributeurs (
    id UUID PRIMARY KEY,
    nom VARCHAR(255),
    prenom VARCHAR(255),
    numero_telephone VARCHAR(64) UNIQUE,
    code_distributeur VARCHAR(128) UNIQUE,
    date_creation TIMESTAMP WITH TIME ZONE
);

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_indexes WHERE tablename = 'distributeurs' AND indexname = 'idx_distributeurs_numero') THEN
        CREATE INDEX idx_distributeurs_numero ON distributeurs(numero_telephone);
    END IF;
END$$;
