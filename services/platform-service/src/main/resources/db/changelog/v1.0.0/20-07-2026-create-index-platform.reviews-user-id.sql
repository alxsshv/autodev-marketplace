-- liquibase formatted sql
-- changeset Aleksey Shvariov:20-07-2026-create-index-platform.reviews-user-id

CREATE INDEX idx_platform_reviews_user_id ON platform.reviews(user_id);

-- rollback DROP INDEX idx_platform_reviews_user_id;