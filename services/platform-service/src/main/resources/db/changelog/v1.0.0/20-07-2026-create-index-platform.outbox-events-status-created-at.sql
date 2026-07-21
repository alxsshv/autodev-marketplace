-- liquibase formatted sql
-- changeset Aleksey Shvariov:20-07-2026-create-index-platform.outbox-events-status-created-at

CREATE INDEX idx_platform_outbox_events_status_created_at ON platform.outbox_events(status, created_at);

-- rollback DROP INDEX idx_platform_outbox_events_status_created_at;