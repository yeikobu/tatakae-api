-- Add apple_sub column to users table for Sign in with Apple authentication
-- This column stores the Apple 'sub' (subject identifier) which uniquely identifies
-- an athlete across iOS and web platforms

ALTER TABLE users
ADD COLUMN apple_sub VARCHAR(255);

-- Add unique constraint to ensure one Apple account = one athlete
CREATE UNIQUE INDEX idx_users_apple_sub ON users(apple_sub) WHERE apple_sub IS NOT NULL;

-- No default value: existing athletes will have NULL apple_sub
-- New athletes created via Apple Sign In will have this field populated
