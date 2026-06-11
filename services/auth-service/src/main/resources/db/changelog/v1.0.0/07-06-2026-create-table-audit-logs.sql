-- liquibase formatted sql
-- changeset Aleksey Shvariov:07-06-2026-create-table-audit-logs

CREATE TABLE auth.audit_logs (
    id          BIGSERIAL       PRIMARY KEY,
    user_id     UUID            NOT NULL,
    event_type  VARCHAR(50)     NOT NULL, -- LOGIN, LOGOUT, TOKEN_REFRESH, INVALID_CREDENTIALS
    ip_address  INET            NULL,
    user_agent  TEXT            NULL,
    created_at  TIMESTAMPTZ     DEFAULT NOW()
);