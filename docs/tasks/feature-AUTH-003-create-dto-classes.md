# Техническое задание: Создание DTO для API endpoints

## Название задачи
Создать DTO классы для API (AUTH-003)

## Название ветки
feature/AUTH-003-create-dto-classes

## Описание
Согласно OpenAPI спецификации (docs/architecture/api-specification/auth-service.yaml), необходимо создать DTO классы для входящих и исходящих данных API endpoints.

## Цель задачи
Создать DTO классы для:
- Запросов (Request DTO)
- Ответов (Response DTO)
- Общих объектов (Shared DTO)

## Критерии выполнения

- [ ] Создан `LoginRequest` DTO с полями:
  - `email` (String, обязательное поле)
  - `password` (String, обязательное поле)

- [ ] Создан `RefreshRequest` DTO с полями:
  - `refreshToken` (String, обязательное поле)

- [ ] Создан `AuthResponse` DTO с полями:
  - `accessToken` (String)
  - `tokenType` (String, enum: "bearer")
  - `expiresIn` (Integer)
  - `refreshToken` (String)

- [ ] Создан `UserDto` DTO с полями:
  - `id` (Long)
  - `keycloakUserId` (String)
  - `email` (String)
  - `firstName` (String, optional)
  - `lastName` (String, optional)
  - `phone` (String, optional)
  - `enabled` (Boolean)
  - `createdAt` (LocalDateTime)

- [ ] Создан `Error` DTO с полями:
  - `error` (String)
  - `message` (String)
  - `timestamp` (LocalDateTime)

- [ ] Создан `WebhookEvent` DTO с полями:
  - `event` (String, enum: "user_created", "user_updated", "user_enabled", "user_disabled", "user_deleted")
  - `timestamp` (LocalDateTime)
  - `user` (Object с полями id, username, email, firstName, lastName, enabled, attributes)

- [ ] Создан `SyncResponse` DTO с полями:
  - `id` (Long)
  - `keycloakUserId` (String)
  - `email` (String)
  - `enabled` (Boolean)
  - `createdAt` (LocalDateTime)

- [ ] Создан `KeycloakPublicKey` DTO для GET /api/v1/auth/keys

- [ ] Создан `KeycloakPublicKeyResponse` DTO с полем `keys` (List<KeycloakPublicKey>)

- [ ] Все DTO классы имеют геттеры и сеттеры (Lombok аннотации `@Data` или `@Getter/@Setter`)

- [ ] Все DTO классы имеют конструкторы (с полным и без параметров)

- [ ] Добавлены Javadoc комментарии для всех DTO классов и полей

## Ссылки
- docs/architecture/api-specification/auth-service.yaml
- docs/coding-standards.md

## Приоритет
Высокий

## Метки
backend, dto, api

## Сложность
Easy

## История изменений
| Версия | Дата | Автор | Описание |
|--------|------|-------|----------|
| 1.0 | 2026-06-14 | Системный аналитик | Создание задачи |
