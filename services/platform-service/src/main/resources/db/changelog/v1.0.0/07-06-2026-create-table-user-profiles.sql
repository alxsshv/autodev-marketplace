-- liquibase forrmatted sql
-- changeset Aleksey Shvariov:07-06-2026-create-table-user-profiles

CREATE TABLE platform.user_profiles (
    id                  BIGSERIAL       PRIMARY KEY,
    keycloak_user_id    VARCHAR(255)    NOT NULL    UNIQUE,
    store_name          VARCHAR(255)    NULL,
    store_description   TEXT            NULL,
    store_logo_url      VARCHAR(255)    NULL,
    avatar_url          VARCHAR(255)    NULL,
    verification_status VARCHAR(50)     NOT NULL    DEFAULT 'PENDING',
    loyalty_balance     NUMERIC(10,2)   NOT NULL    DEFAULT 0,
    created_at          TIMESTAMP       NOT NULL    DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP       NOT NULL    DEFAULT CURRENT_TIMESTAMP
);

-- rollback DROP TABLE platform.user_profiles;