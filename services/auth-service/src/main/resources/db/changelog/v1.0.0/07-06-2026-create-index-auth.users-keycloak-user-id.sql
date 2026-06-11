-- liquibase formatted sql
-- changeset Alkesey Shvariov:07-06-2026-create-indexes-auth.users-keycloak-user-id

CREATE INDEX idx_auth_users_keycloak_user_id ON auth.users(keycloak_user_id);

-- rollback DROP INDEX idx_auth_users_keycloak_user_id;