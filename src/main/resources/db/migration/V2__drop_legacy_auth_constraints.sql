-- Make existing legacy dummy-auth columns non-blocking for OTP-only auth.
-- If the old schema had NOT NULL on username/password_hash, OTP inserts would fail.

DO $$
BEGIN
  IF EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_name = 'users' AND column_name = 'password_hash'
  ) THEN
    EXECUTE 'ALTER TABLE users ALTER COLUMN password_hash DROP NOT NULL';
  END IF;

  IF EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_name = 'users' AND column_name = 'username'
  ) THEN
    EXECUTE 'ALTER TABLE users ALTER COLUMN username DROP NOT NULL';
  END IF;
END $$;

