-- liquibase formatted sql
-- changeset Aleksey Shvariov:07-06-2026-create-index-auth.audit-logs-event-type

CREATE INDEX idx_auth_audit_logs_event_type ON auth.audit_logs(event_type);

-- roolback DROP INDEX idx_auth_audit_logs_event_type;