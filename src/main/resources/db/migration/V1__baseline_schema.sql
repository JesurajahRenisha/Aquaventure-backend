-- Baseline: reproduces the schema as it existed before Flyway was introduced
-- (originally created by Hibernate ddl-auto=update). On an existing dev database
-- this version is recorded via spring.flyway.baseline-on-migrate without running;
-- on a fresh database it creates the schema from scratch.

CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    firstname VARCHAR(255),
    lastname VARCHAR(255),
    email VARCHAR(255),
    password VARCHAR(255)
);

ALTER TABLE users ADD CONSTRAINT uq_users_email UNIQUE (email);

CREATE TABLE IF NOT EXISTS customer (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255),
    email VARCHAR(255)
);
