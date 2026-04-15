-- OTP-based authentication schema migration.
-- Runs automatically via Flyway on application startup.

-- Create users table if missing; otherwise evolve it to OTP-based structure.
CREATE TABLE IF NOT EXISTS users (
  id BIGSERIAL PRIMARY KEY
);

ALTER TABLE users
  ADD COLUMN IF NOT EXISTS mobile_number VARCHAR(20);

ALTER TABLE users
  ADD COLUMN IF NOT EXISTS name VARCHAR(120);

ALTER TABLE users
  ADD COLUMN IF NOT EXISTS email VARCHAR(200);

ALTER TABLE users
  ADD COLUMN IF NOT EXISTS is_verified BOOLEAN;

ALTER TABLE users
  ADD COLUMN IF NOT EXISTS created_at TIMESTAMPTZ;

ALTER TABLE users
  ADD COLUMN IF NOT EXISTS updated_at TIMESTAMPTZ;

-- Remove legacy constraints if present (best-effort).
ALTER TABLE users
  ALTER COLUMN mobile_number SET NOT NULL;

-- Ensure unique mobile_number
DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM pg_constraint WHERE conname = 'uk_users_mobile_number'
  ) THEN
    ALTER TABLE users ADD CONSTRAINT uk_users_mobile_number UNIQUE (mobile_number);
  END IF;
END $$;

-- OTP table
CREATE TABLE IF NOT EXISTS otps (
  id BIGSERIAL PRIMARY KEY,
  mobile_number VARCHAR(20) NOT NULL,
  otp VARCHAR(10) NOT NULL,
  expiry_time TIMESTAMP NOT NULL,
  is_used BOOLEAN NOT NULL DEFAULT FALSE,
  attempt_count INTEGER NOT NULL DEFAULT 0,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_otps_mobile_number ON otps(mobile_number);

