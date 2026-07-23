-- liquibase formatted sql
-- changeset Aleksey Shvariov:20-07-2026-create-table-reviews

CREATE TABLE platform.reviews (
id              UUID        PRIMARY KEY                 DEFAULT gen_random_uuid(),
product_id      UUID                        NOT NULL,
user_id         UUID                        NOT NULL,
rating          SMALLINT                    NOT NULL    CHECK (rating BETWEEN 1 AND 5),
review_text     TEXT,
seller_reply    TEXT,
created_at      TIMESTAMP WITH TIME ZONE    NOT NULL    DEFAULT NOW(),
updated_at      TIMESTAMP WITH TIME ZONE    NOT NULL    DEFAULT NOW()
)

-- rollback DROP TABLE platform.reviews;