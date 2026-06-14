# Техническое задание: Реализация AuthController

## Название задачи
Реализовать AuthController (AUTH-007)

## Название ветки
feature/AUTH-007-implement-auth-controller

## Описание
AuthController - REST API контроллер для обработки HTTP запросов к API endpoints аутентификации.

## Цель задачи
Реализовать AuthController с endpoints:
- POST /api/v1/auth/login
- POST /api/v1/auth/refresh
- POST /api/v1/auth/logout
- GET /api/v1/auth/me
- GET /api/v1/auth/keys
- POST /api/v1/auth/sync

## Критерии выполнения

- [ ] Создан контроллер `AuthController` с аннотацией `@RestController` и `@RequestMapping("/api/v1/auth")`

- [ ] Реализован endpoint `POST /login`:
  - Принимает `LoginRequest`
  - Вызывает `AuthService.login()`
  - Возвращает `AuthResponse`
  - Обработка ошибок (401, 429)

- [ ] Реализован endpoint `POST /refresh`:
  - Принимает `RefreshRequest`
  - Вызывает `AuthService.refresh()`
  - Возвращает `AuthResponse`
  - Обработка ошибок (401)

- [ ] Реализован endpoint `POST /logout`:
  - Защищен аннотацией `@PreAuthorize("hasAnyRole('BUYER', 'SELLER', 'MODERATOR', 'ADMIN')")`
  - Вызывает `AuthService.logout()`
  - Возвращает HTTP 204 No Content
  - Обработка ошибок (401)

- [ ] Реализован endpoint `GET /me`:
  - Защищен аннотацией `@PreAuthorize("hasAnyRole('BUYER', 'SELLER', 'MODERATOR', 'ADMIN')")`
  - Вызывает `AuthService.getCurrentUser()`
  - Возвращает `UserDto`
  - Обработка ошибок (401)

- [ ] Реализован endpoint `GET /keys`:
  - Публичный endpoint (без авторизации)
  - Вызывает `AuthService.getKeycloakPublicKeys()`
  - Возвращает `KeycloakPublicKeyResponse`

- [ ] Реализован endpoint `POST /sync`:
  - Публичный endpoint для webhook от Keycloak
  - Принимает `WebhookEvent`
  - Вызывает `AuthService.syncUser()`
  - Возвращает HTTP 200 OK
  - Обработка ошибок (400, 500)

- [ ] Все endpoints покрыты интеграционными тестами

- [ ] Добавлены Javadoc комментарии для всех методов

## Ссылки
- docs/architecture/api-specification/auth-service.yaml
- docs/architecture/security/rbac.md

## Приоритет
Высокий

## Метки
backend, controller, api

## Сложность
Medium

## История изменений
| Версия | Дата | Автор | Описание |
|--------|------|-------|----------|
| 1.0 | 2026-06-14 | Системный аналитик | Создание задачи |
