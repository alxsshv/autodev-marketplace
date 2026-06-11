-- liquibase formatted sql
-- changeset Alkesey Shvariov:07-06-2026-create-indexes-auth.users-email

CREATE INDEX idx_auth_users_email ON auth.users(email);

-- rollback DROP INDEX idx_auth_users_email;