-- liquibase formatted sql
-- changeset Aleksey Shvariov:07-06-2026-create-indexes-auth.users-owner-id

CREATE INDEX idx_auth_users_owner_id ON auth.users(owner_id);

-- rollback DROP INDEX idx_auth_users_owner_id;