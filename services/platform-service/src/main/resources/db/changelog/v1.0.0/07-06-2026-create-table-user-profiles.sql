-- liquibase forrmatted sql
-- changeset Aleksey Shvariov:07-06-2026-create-table-user-profiles

CREATE TABLE platform_service.user_profiles (
    id                  BIGSERIAL       PRIMARY KEY,
    user_id             BIGINT          NOT NULL    UNIQUE,
    keycloak_user_id    VARCHAR(255)    NOT NULL    UNIQUE,
    store_name          VARCHAR(255)    NULL,
    store_description   TEXT            NULL,
    store_logo_url      VARCHAR(255)    NULL,
    verification_status VARCHAR(50)     NOT NULL    DEFAULT 'PENDING'
    loyality_balance    NUMERIC(10,2)   NOT NULL    DEFAULT 0,
    created_at          TIMESTAMP       NOT NULL    DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP       NOT NULL    DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES auth.user(id) ON DELETE CASACADE
);

-- rollback DROP TABLE platform_service.user_profiles;