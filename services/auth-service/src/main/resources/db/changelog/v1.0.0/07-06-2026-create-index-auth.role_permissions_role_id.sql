-- liquibase formatted sql
-- changeset Aleksey Shvariov:07-06-2026-create-index-auth.role_permissions_role_id

CREATE INDEX idx_auth_role_permissions_role_id ON auth.role_permissions(role_id);

-- roolback DROP INDEX idx_auth_role_permissions_role_id;