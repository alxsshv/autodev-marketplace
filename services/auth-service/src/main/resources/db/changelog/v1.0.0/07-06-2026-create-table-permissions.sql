-- liquibase formatted sql
-- changeset Alkesey Shvariov:07-06-2026-create-table-permissions

CREATE TABLE auth.permissions (
    id          BIGSERIAL       PRIMARY KEY,
    name        VARCHAR(100)    NOT NULL    UNIQUE,
    description VARCHAR(255)    NULL,
    created_at  TIMESTAMP       NOT NULL    DEFAULT CURRENT_TIMESTAMP
);

-- rollback DROP TABLE auth.permissions;