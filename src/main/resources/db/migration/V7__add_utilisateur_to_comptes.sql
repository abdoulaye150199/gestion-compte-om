-- Add utilisateur_id to comptes table to link account to user
ALTER TABLE comptes
    ADD COLUMN utilisateur_id UUID;

ALTER TABLE comptes
    ADD CONSTRAINT fk_comptes_utilisateur FOREIGN KEY (utilisateur_id) REFERENCES utilisateurs(id);

CREATE INDEX idx_comptes_utilisateur_id ON comptes(utilisateur_id);
