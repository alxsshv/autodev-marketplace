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
  - `enabled` (Boolean)
  - `createdAt` (LocalDateTime)

- [ ] Создан `Error` DTO с полями:
  - `error` (String)
  - `message` (String)
  - `timestamp` (LocalDateTime)

- [ ] Создан `WebhookEvent` DTO с полями:
  - `event` (String, enum: "user_created", "user_updated", "user_enabled", "user_disabled", "user_deleted")
  - `timestamp` (LocalDateTime)
  - `user` (Object с полями id, username, email, enabled)
  
**Примечание:** Значения enum указаны в snake_case как в OpenAPI спецификации. Поле `user` имеет inline определение типа (не ссылается на UserDto), так как в спецификации определено напрямую в схеме WebhookEvent.

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
| 1.1 | 2026-06-17 | Системный аналитик | Убраны несоответствующие поля (firstName, lastName, phone, attributes) из UserDto и WebhookEvent. Приведены в соответствие с сущностью User. |
| 1.2 | 2026-06-17 | Системный аналитик | Уточнено: значения enum для event используют snake_case (user_created, user_updated...) согласно OpenAPI спецификации. |
| 1.3 | 2026-06-17 | Системный аналитик | Уточнено: поле user в WebhookEvent имеет inline определение (не ссылается на UserDto), так как в спецификации определено напрямую в схеме WebhookEvent. |
| 1.4 | 2026-06-17 | Системный аналитик | Добавлен код Java для WebhookEvent с вложенным record User. Поле user имеет другую структуру, чем UserDto (id, username, email, enabled). |
| 1.5 | 2026-06-17 | Системный аналитик | Удален SyncResponse. Он определен в спецификации, но не используется в текущей версии эндпоинта POST /api/v1/auth/sync, который возвращает только HTTP статус без тела ответа. |
| 1.6 | 2026-06-17 | Системный аналитик | Удалены KeycloakPublicKey и KeycloakPublicKeyResponse. Эндпоинт GET /api/v1/auth/keys использует inline schema в спецификации. |
