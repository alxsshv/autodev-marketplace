-- liquibase formatted sql
-- changeset Alkesey Shvariov:07-06-2026-create-indexes-auth.roles-name

CREATE INDEX idx_roles_name ON auth.roles(name);

-- rollback DROP INDEX idx_roles_name;