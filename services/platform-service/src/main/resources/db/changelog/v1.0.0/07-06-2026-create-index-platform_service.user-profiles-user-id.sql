-- liquibase formatted sql
-- changeset Aleksey Shvariov:07-06-2026-create-index-platform_service.user_profiles-user-id

CREATE INDEX idx_platform_service_user_profiles_user_id ON platform_service.user_profiles(user_id);

-- rollback DROP INDEX idx_platform_service_user_profiles_user_id;