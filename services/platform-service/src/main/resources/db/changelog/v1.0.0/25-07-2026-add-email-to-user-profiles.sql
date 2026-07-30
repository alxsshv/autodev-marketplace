-- liquibase formatted sql
-- changeset Aleksey Shvariov:25-07-2026-add-email-to-user-profiles

ALTER TABLE platform.user_profiles
ADD COLUMN email VARCHAR(255) NOT NULL;

CREATE UNIQUE INDEX idx_user_profiles_email ON platform.user_profiles(email);

-- rollback DROP INDEX idx_user_profiles_email;
-- rollback ALTER TABLE platform.user_profiles DROP COLUMN email;
