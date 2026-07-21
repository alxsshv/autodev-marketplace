-- liquibase formatted sql
-- changeset Aleksey Shvariov:07-06-2026-create-index-platform-service.user-profiles-verification

CREATE INDEX idx_platform_user_profiles_verification ON platform.user_profiles(verification_status);

-- roolback DROP INDEX idx_platform_user_profiles_verification;