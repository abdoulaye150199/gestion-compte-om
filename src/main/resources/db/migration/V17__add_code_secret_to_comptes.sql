-- Add code_secret field to comptes to store the account secret/code
ALTER TABLE comptes
    ADD COLUMN code_secret VARCHAR(255);
