-- liquibase formatted sql
-- changeset Aleksey Shvariov:07-06-2026-create-index-auth.providers-user-provider-id

CREATE INDEX idx_auth_providers_user_provider_id ON auth.oauth_providers(provider_id);

-- roolback DROP INDEX idx_auth_providers_user_provider_id;