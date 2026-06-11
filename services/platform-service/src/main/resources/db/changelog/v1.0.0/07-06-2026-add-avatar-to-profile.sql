--liquibase formatted sql
--changeset Aleksey Shvariov:07-06-2026-add-avatar-to-profile

ALTER TABLE platform_service.user_profiles ADD COLUMN avatar_url VARCHAR(255) NULL;
CREATE INDEX idx_user_profiles_avatar ON platform_service.user_profiles(avatar_url);

--rollback ALTER TABLE platform_service.user_profiles DROP COLUMN avatar_url;
--rollback DROP INDEX idx_user_profiles_avatar;