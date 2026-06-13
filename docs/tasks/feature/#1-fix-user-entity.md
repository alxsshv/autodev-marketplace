# Задача 1: Исправить сущность UserEntity для соответствия модели данных

## Название задачи
Исправить сущность UserEntity для соответствия модели данных (только аутентификационные данные)

## Описание
Согласно `docs/architecture/data-model.md`, таблица `auth.users` должна содержать ТОЛЬКО аутентификационные данные:
- id (первичный ключ)
- keycloak_user_id (ID пользователя в Keycloak)
- email (для аутентификации)
- enabled (активен ли пользователь)
- created_at (дата создания)

**ТЕКУЩЕЕ СОСТОЯНИЕ:** В сущности User.java есть бизнес-данные (first_name, last_name, phone, role), которые должны быть удалены.

**ПОЧЕМУ:** Согласно архитектуре AutoDev Marketplace, auth-service управляет ТОЛЬКО аутентификационными данными. Все бизнес-данные пользователя хранятся в `platform_service.users`.

## Критерии выполнения

- [ ] Удалены поля: first_name, last_name, phone, role из User.java
- [ ] Оставлены поля: id, keycloak_user_id, email, enabled, created_at
- [ ] Класс User.java соответствует миграции `03-06-2026-create-table-users.sql`
- [ ] Удалены связи с другими сущностями (если есть)

## Ссылки
- [docs/architecture/data-model.md](../../docs/architecture/data-model.md) - раздел "Схема: auth"
- [docs/architecture/glossary.md](../../docs/architecture/glossary.md) - соглашения по модели данных
- [services/auth-service/src/main/resources/db/changelog/v1.0.0/03-06-2026-create-table-users.sql](../../services/auth-service/src/main/resources/db/changelog/v1.0.0/03-06-2026-create-table-users.sql)

## Приоритет
Высокий

## Метки
backend, database, entity, auth-service
