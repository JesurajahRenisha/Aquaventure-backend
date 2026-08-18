-- Replaces the `admin` profile table (previously a 1:1 link to `users`,
-- populated via role-based registration) with a standalone admin table.
-- Admin accounts are no longer tied to `users` / self-registration; they
-- are inserted manually (e.g. directly via SQL) with just an email and a
-- bcrypt password hash, and authenticate through a separate admin login.

DROP TABLE IF EXISTS admin;

CREATE TABLE admin (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(150) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL
);
