-- Create accounts for vendeurs using their merchant codes as account numbers
-- This migration is idempotent and safe to run multiple times
-- Note: This runs before seeding, so it will be empty initially
-- A trigger or scheduled task could be added to create accounts when vendeurs are added

-- For now, we'll create the accounts manually after seeding in the application startup
-- This migration serves as a placeholder and documentation