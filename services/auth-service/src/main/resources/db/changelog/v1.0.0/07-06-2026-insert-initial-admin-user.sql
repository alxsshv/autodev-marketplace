--liquibase formatted sql
--changeset Aleksey Shvariov:07-06-2026-insert-initial-admin-user

-- Вставка начального администратора (email: admin@autodev.local)
-- keycloak_user_id должен быть заменён на реальный ID администратора в Keycloak
INSERT INTO auth.users (keycloak_user_id, email, first_name, last_name, role, enabled)
VALUES ('admin-keycloak-id', 'admin@autodev.local', 'System', 'Admin', 'ADMIN', TRUE)
ON CONFLICT (keycloak_user_id) DO NOTHING;

--rollback DELETE FROM auth.users WHERE email = 'admin@autodev.local';