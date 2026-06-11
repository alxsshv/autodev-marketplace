-- liquibase formatted sql
-- changeset Aleksey Shvariov:07-06-2026-create-index-auth.audit-logs-created-at

CREATE INDEX idx_auth_audit_logs_created_at ON auth.audit_logs(created_at);

-- roolback DROP INDEX idx_auth_audit_logs_created_at;