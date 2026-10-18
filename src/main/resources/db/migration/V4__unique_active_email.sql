-- Emails are unique per ACTIVE user and case-insensitive.
-- The old constraint covered soft-deleted rows too, so a deleted account blocked its email forever,
-- and it was case-sensitive (Fulano@x.com and fulano@x.com could both sign up).

ALTER TABLE users DROP CONSTRAINT IF EXISTS uq_users_email;

-- The API now stores emails trimmed and lowercase; bring existing rows in line.
UPDATE users SET email = lower(trim(email)) WHERE email <> lower(trim(email));

CREATE UNIQUE INDEX IF NOT EXISTS uq_users_email_active
ON users (lower(email))
WHERE deleted_at IS NULL;
