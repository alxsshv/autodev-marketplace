-- liquibase formatted sql
-- changeset Aleksey Shvariov:20-07-2026-create-table-outbox

CREATE TABLE platform.outbox_events (
id                  UUID            PRIMARY KEY                 DEFAULT gen_random_uuid(),
aggregate_type      VARCHAR(255)                    NOT NULL,
aggregate_id        UUID                            NOT NULL,
event_type          VARCHAR(255)                    NOT NULL,
payload             JSONB                           NOT NULL,
created_at          TIMESTAMP WITH TIME ZONE        NOT NULL    DEFAULT now(),
status              VARCHAR(50)                     NOT NULL    DEFAULT 'PENDING'
);

-- rollback DROP TABLE platform.outbox_events;
