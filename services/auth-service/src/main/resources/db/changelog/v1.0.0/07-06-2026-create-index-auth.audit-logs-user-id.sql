-- liquibase formatted sql
-- changeset Aleksey Shvariov:07-06-2026-create-index-auth.audit-logs-user-id

CREATE INDEX idx_auth_audit_logs_user_id ON auth.audit_logs(user_id);

-- roolback DROP INDEX idx_auth_audit_logs_user_id;