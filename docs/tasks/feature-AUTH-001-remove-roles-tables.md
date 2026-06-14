# Техническое задание: Удаление таблиц ролей (auth.roles, auth.permissions, auth.role_permissions)

## Название задачи
Удалить таблицы ролей и их индексы (AUTH-001)

## Название ветки
feature/AUTH-001-remove-roles-tables

## Описание
Согласно архитектуре проекта (docs/architecture/data-model.md, docs/architecture/security/rbac.md), таблицы `auth.roles`, `auth.permissions`, `auth.role_permissions` НЕ ДОЛЖНЫ существовать для MVP. Роли пользователей хранятся исключительно в Keycloak и передаются в JWT токене.

В текущем состоянии проекта эти таблицы созданы в миграциях, что нарушает архитектурные принципы.

## Цель задачи
Удалить таблицы ролей и их индексы из БД и обновить миграции.

## Критерии выполнения

- [ ] Удалены следующие таблицы:
  - `auth.roles`
  - `auth.permissions`
  - `auth.role_permissions`

- [ ] Удалены все индексы, связанные с этими таблицами (см. список ниже)

- [ ] Удалены все ограничения внешних ключей, ссылающихся на удаленные таблицы

- [ ] Обновлен master changelog (db.changelog-master.yaml) - удалены ссылки на миграции удаленных таблиц

- [ ] Обновлены все ссылки на удаленные таблицы в документации

## Индексы для удаления
- `idx_auth_role_permissions_permission_id`
- `idx_auth_role_permissions_role_id`
- `idx_auth_roles_name`
- `idx_auth_permissions_name`
- `idx_auth_permissions_code`
- `idx_auth_oauth_providers_provider`
- `idx_auth_oauth_providers_user`
- `idx_auth_oauth_providers_user_provider`
- `idx_auth_providers_user_provider`
- `idx_auth_providers_user_provider_id`
- `idx_auth_audit_logs_created_at`
- `idx_auth_audit_logs_event_type`
- `idx_auth_audit_logs_user_id`

## Ссылки
- docs/architecture/data-model.md - раздел "ВАЖНО: Роли пользователей хранятся исключительно в Keycloak"
- docs/architecture/security/rbac.md - раздел "Единственный источник правды"
- docs/architecture/glossary.md - раздел "Single Source of Truth"

## Приоритет
Высокий (нарушение архитектуры)

## Метки
backend, database, architecture, critical

## Сложность
Easy

## История изменений
| Версия | Дата | Автор | Описание |
|--------|------|-------|----------|
| 1.0 | 2026-06-14 | Системный аналитик | Создание задачи |
