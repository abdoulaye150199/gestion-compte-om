-- Seed some vendeurs (sellers/merchants) for development/testing
INSERT INTO vendeurs (id, nom, prenom, numero_telephone, code_marchant, date_creation)
VALUES
    ('33333333-4444-5555-6666-777777777777', 'Martin', 'Pierre', '+221780000001', 'MARCHAND-001', now()),
    ('44444444-5555-6666-7777-888888888888', 'Dubois', 'Sophie', '+221780000002', 'MARCHAND-002', now())
ON CONFLICT (numero_telephone) DO UPDATE SET
    nom = EXCLUDED.nom,
    prenom = EXCLUDED.prenom,
    code_marchant = EXCLUDED.code_marchant;