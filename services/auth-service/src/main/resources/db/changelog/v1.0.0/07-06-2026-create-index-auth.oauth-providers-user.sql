-- liquibase formatted sql
-- changeset Aleksey Shvariov:07-06-2026-create-index-auth.oauth-providers-user

CREATE INDEX idx_auth_oauth_providers_user ON auth.oauth_providers(user_id);

-- roolback DROP INDEX idx_auth_oauth_providers_user;