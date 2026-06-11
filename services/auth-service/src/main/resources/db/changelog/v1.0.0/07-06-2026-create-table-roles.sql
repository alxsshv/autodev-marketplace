-- liquibase formatted sql
-- changeset Aleksey Shvariov:07-06-2026-create-table-roles

CREATE TABLE auth.roles (
    id          BIGSERIAL       PRIMARY KEY,
    name        VARCHAR(50)     NOT NULL    UNIQUE,
    description VARCHAR(255)    NULL        UNIQUE,
    created_at  TIMESTAMP       NOT NULL    DEFAULT CURRENT_TIMESTAMP
);

-- rollback DROP TABLE auth.roles;