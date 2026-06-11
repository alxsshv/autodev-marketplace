-- liquibase formatted sql
-- changeset Aleksey Shvariov:07-06-2026-create-index-auth.oauth-providers-user-provider

CREATE INDEX idx_auth_oauth_providers_user_provider ON auth.oauth_providers(user_id, provider);

-- rollback DROP INDEX idx_auth_oauth_providers_user_provider;