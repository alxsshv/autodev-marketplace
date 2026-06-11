-- liquibase formatted sql
-- changeset Aleksey Shvariov:07-06-2026-create-table-role-permissions

CREATE TABLE auth.role_permissions (
    role_id         BIGINT          NOT NULL,
    permission_id   BIGINT          NOT NULL,
    PRIMARY KEY (role_id, permission_id),
    FOREIGN KEY (role_id) REFERENCES auth.roles(id) ON DELETE CASCADE,
    FOREIGN KEY (permission_id) REFERENCES auth.permissions(id) ON DELETE CASCADE
);

-- rollback DROP TABLE auth.role_permissions;