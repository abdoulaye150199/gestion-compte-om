-- Create table for vendeurs (sellers/merchants)
CREATE TABLE IF NOT EXISTS vendeurs (
    id UUID PRIMARY KEY,
    nom VARCHAR(255),
    prenom VARCHAR(255),
    numero_telephone VARCHAR(64) UNIQUE,
    code_marchant VARCHAR(128) UNIQUE,
    date_creation TIMESTAMP WITH TIME ZONE
);

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_indexes WHERE tablename = 'vendeurs' AND indexname = 'idx_vendeurs_numero') THEN
        CREATE INDEX idx_vendeurs_numero ON vendeurs(numero_telephone);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_indexes WHERE tablename = 'vendeurs' AND indexname = 'idx_vendeurs_code_marchant') THEN
        CREATE INDEX idx_vendeurs_code_marchant ON vendeurs(code_marchant);
    END IF;
END$$;