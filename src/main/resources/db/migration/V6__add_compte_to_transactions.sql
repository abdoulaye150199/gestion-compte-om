ALTER TABLE transactions
    ADD COLUMN IF NOT EXISTS compte_id UUID;

ALTER TABLE transactions
    ADD CONSTRAINT IF NOT EXISTS fk_transactions_compte FOREIGN KEY (compte_id) REFERENCES comptes(id);

-- create index to speed up lookups by compte
CREATE INDEX IF NOT EXISTS idx_transactions_compte_id ON transactions(compte_id);
