# Техническое задание: Реализация AuthService

## Название задачи
Реализовать AuthService (AUTH-006)

## Название ветки
feature/AUTH-006-implement-auth-service

## Описание
AuthService - основной сервис для обработки логики аутентификации, обновления токенов, выхода из системы и получения данных текущего пользователя.

## Цель задачи
Реализовать AuthService с методами:
- `login()` - аутентификация пользователя
- `refresh()` - обновление токена
- `logout()` - выход из системы
- `getCurrentUser()` - получение данных текущего пользователя
- `getKeycloakPublicKeys()` - получение публичных ключей Keycloak
- `syncUser()` - синхронизация пользователя из Keycloak

## Критерии выполнения

- [ ] Реализован метод `login(String email, String password)`:
  - Валидация учетных данных
  - Генерация JWT токенов (access + refresh)
  - Возврат `AuthResponse`
  - Обработка ошибок (неверные учетные данные, заблокированный пользователь)

- [ ] Реализован метод `refresh(String refreshToken)`:
  - Валидация refresh token
  - Генерация новых JWT токенов
  - Возврат `AuthResponse`
  - Обработка ошибок (неверный refresh token)

- [ ] Реализован метод `logout()`:
  - Добавление access token в Redis blacklist
  - Удаление refresh token из Redis
  - Возврат HTTP 204 No Content

- [ ] Реализован метод `getCurrentUser()`:
  - Получение данных текущего пользователя из Redis кэша
  - Возврат `UserDto`

- [ ] Реализован метод `getKeycloakPublicKeys()`:
  - Получение публичных ключей Keycloak через HTTP client
  - Возврат `KeycloakPublicKeyResponse`

- [ ] Реализован метод `syncUser(WebhookEvent event)`:
  - Обработка событий от Keycloak (user_created, user_updated, user_enabled, user_disabled)
  - Создание/обновление пользователя в PostgreSQL
  - Отправка события в Kafka

- [ ] Все методы имеют обработку исключений

- [ ] Все методы покрыты модульными тестами с моками репозиториев

- [ ] Добавлены Javadoc комментарии для всех методов

## Ссылки
- docs/architecture/api-specification/auth-service.yaml
- docs/architecture/security/user-registration-architecture.md
- docs/architecture/security/revoked-tokens.md

## Приоритет
Высокий

## Метки
backend, service, business-logic

## Сложность
Medium

## История изменений
| Версия | Дата | Автор | Описание |
|--------|------|-------|----------|
| 1.0 | 2026-06-14 | Системный аналитик | Создание задачи |
