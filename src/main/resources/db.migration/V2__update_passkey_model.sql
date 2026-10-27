ALTER TABLE passkeys DROP CONSTRAINT <the unique constraint on user_id>;
ALTER TABLE passkeys
  ADD COLUMN uv_initialized boolean NOT NULL DEFAULT false,
  ADD COLUMN backup_eligible boolean NOT NULL DEFAULT false,
  ADD COLUMN backup_state boolean NOT NULL DEFAULT false;