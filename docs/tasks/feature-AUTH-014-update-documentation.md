# Техническое задание: Обновление документации Auth Service

## Название задачи
Обновить документацию (AUTH-014)

## Название ветки
feature/AUTH-014-update-documentation

## Описание
Необходимо обновить документацию, чтобы она соответствовала реализованному функционалу.

## Цель задачи
Обновить документацию Auth Service с учетом всех изменений.

## Критерии выполнения

- [ ] Обновлен README.md auth-service с описанием всех endpoints

- [ ] Обновлена OpenAPI спецификация (docs/architecture/api-specification/auth-service.yaml) с примерами запросов и ответов

- [ ] Добавлена документация по API endpoints:
  - `/api/v1/auth/login` - пример запроса и ответа
  - `/api/v1/auth/refresh` - пример запроса и ответа
  - `/api/v1/auth/logout` - пример запроса (с токеном)
  - `/api/v1/auth/me` - пример ответа
  - `/api/v1/auth/keys` - пример ответа с публичными ключами
  - `/api/v1/auth/sync` - пример webhook от Keycloak

- [ ] Обновлен раздел RBAC с учетом того, что роли хранятся только в Keycloak

- [ ] Обновлен раздел синхронизации с Keycloak

- [ ] Добавлен раздел миграций с описанием удаления таблиц ролей

- [ ] Обновлен glossary с учетом новых терминов

## Ссылки
- docs/architecture/api-specification/auth-service.yaml
- docs/architecture/security/rbac.md
- docs/architecture/security/user-registration-architecture.md

## Приоритет
Средний

## Метки
docs, documentation

## Сложность
Easy

## История изменений
| Версия | Дата | Автор | Описание |
|--------|------|-------|----------|
| 1.0 | 2026-06-14 | Системный аналитик | Создание задачи |
