--liquibase formatted sql
--changeset Aleksey Shvariov:03-06-2026-create-table-users

CREATE TABLE auth.users (
    id                  BIGSERIAL      PRIMARY KEY,
    keycloak_user_id    VARCHAR(255)   NOT NULL   UNIQUE,
    email               VARCHAR(255)   NOT NULL   UNIQUE,
    enabled             BOOLEAN        NOT NULL   DEFAULT TRUE,
    created_at          TIMESTAMP      NOT NULL   DEFAULT CURRENT_TIMESTAMP
    );

--rollback DROP TABLE auth.users;
