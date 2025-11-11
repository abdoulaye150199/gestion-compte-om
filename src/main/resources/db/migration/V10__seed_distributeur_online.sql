-- Idempotent seeder for online DB: upsert the distributeur record requested by the user
-- This file is safe to run multiple times. It uses ON CONFLICT (numero_telephone) DO UPDATE.

INSERT INTO distributeurs (id, nom, prenom, numero_telephone, code_distributeur, date_creation)
VALUES (
  '11111111-2222-3333-4444-555555555555',
  'Diallo',
  'Laye',
  '+221780000000',
  'dist-1234',
  now()
)
ON CONFLICT (numero_telephone) DO UPDATE SET
  nom = EXCLUDED.nom,
  prenom = EXCLUDED.prenom,
  code_distributeur = EXCLUDED.code_distributeur,
  date_creation = COALESCE(distributeurs.date_creation, EXCLUDED.date_creation);

-- You can add further seed rows below following the same pattern.
