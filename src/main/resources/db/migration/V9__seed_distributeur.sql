-- Seed a distributor for development/testing
-- Requested distributor: Diallo Laye, phone +221780000000, code dist-1234
INSERT INTO distributeurs (id, nom, prenom, numero_telephone, code_distributeur, date_creation)
VALUES ('11111111-2222-3333-4444-555555555555', 'Diallo', 'Laye', '+221780000000', 'dist-1234', now())
ON CONFLICT (numero_telephone) DO UPDATE SET nom = EXCLUDED.nom, prenom = EXCLUDED.prenom, code_distributeur = EXCLUDED.code_distributeur;
