-- liquibase formatted sql
-- changeset Aleksey Shvariov:07-06-2026-create-index-auth.providers-user-provider

CREATE INDEX idx_auth_providers_user_provider ON auth.oauth_providers(provider);

-- roolback DROP INDEX idx_auth_providers_user_provider;