-- liquibase formatted sql
-- changeset Aleksey Shvariov:07-06-2026-create-index-platform-service.user-profiles-user-id

CREATE INDEX idx_platform_user_profiles_user_id ON platform.user_profiles(user_id);

-- roolback DROP INDEX idx_platform_user_profiles_user_id;