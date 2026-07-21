-- liquibase formatted sql
-- changeset Aleksey Shvariov:20-07-2026-create-index-platform.reviews-product-id

CREATE INDEX idx_platform_reviews_product_id ON platform.reviews(product_id);

-- rollback DROP INDEX idx_platform_reviews_product_id;