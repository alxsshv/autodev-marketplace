-- liquibase formatted sql
-- changeset Aleksey Shveriov:03-06-2026-create-table-oauth-providers

CREATE TABLE auth.oauth_providers (
    id              BIGSERIAL       PRIMARY KEY,
    user_id         BIGINT          NOT NULL   REFERENCES auth.users(id) ON DELETE CASCADE,
    provider        VARCHAR(50)     NOT NULL,
    provider_id     VARCHAR(255)    NOT NULL,
    access_token    TEXT            NULL,
    refresh_token   TEXT            NULL,
    expires_at      TIMESTAMP       NULL,
    scopes          TEXT[]          NULL,
    last_sync_at    TIMESTAMP       NULL,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, provider),
    UNIQUE(provider, provider_id)
);

-- rollback DROP TABLE auth.oauth_providers;