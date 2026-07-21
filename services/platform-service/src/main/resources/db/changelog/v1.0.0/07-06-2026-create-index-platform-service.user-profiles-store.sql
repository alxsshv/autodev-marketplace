-- liquibase formatted sql
-- changeset Aleksey Shvariov:07-06-2026-create-index-platform-service.user-profiles-store

CREATE INDEX idx_platform_user_profiles_store ON platform.user_profiles(store_name);

-- roolback DROP INDEX idx_platform_user_profiles_store;