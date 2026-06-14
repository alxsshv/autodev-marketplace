# Техническое задание: Настройка Spring Security для Auth Service

## Название задачи
Настроить Spring Security (AUTH-008)

## Название ветки
feature/AUTH-008-setup-spring-security

## Описание
Необходимо настроить Spring Security для проверки JWT токенов и управления доступом к endpoints на основе ролей (RBAC).

## Цель задачи
Настроить Spring Security с:
- JWT аутентификацией
- RBAC (ROLE_BUYER, ROLE_SELLER, ROLE_MODERATOR, ROLE_ADMIN)
- Конвертером JWT в Spring Authorities

## Критерии выполнения

- [ ] Создана конфигурация `SecurityConfig` с аннотацией `@Configuration` и `@EnableWebSecurity`

- [ ] Настроен `SecurityFilterChain`:
  - CSRF отключен
  - Все endpoints `/api/v1/**` требуют аутентификации
  - Используется OAuth2 Resource Server с JWT

- [ ] Настроен `JwtDecoder`:
  - Использует публичные ключи из Keycloak
  - Проверяет `iss`, `aud`, `exp` claims

- [ ] Настроен `JwtAuthenticationConverter`:
  - Маппит `realm_access.roles` в Spring Authorities
  - Префикс `ROLE_` для ролей

- [ ] Создан `JwtValidator`:
  - Проверяет валидность токена
  - Проверяет revoked токены через Redis

- [ ] Все endpoints защищены аннотациями `@PreAuthorize` с соответствующими ролями:
  - `/login`, `/refresh`, `/keys`, `/sync` - публичные (без авторизации)
  - `/logout`, `/me` - BUYER, SELLER, MODERATOR, ADMIN

- [ ] Обработка ошибок аутентификации (401 Unauthorized) и авторизации (403 Forbidden)

- [ ] Конфигурация покрыта интеграционными тестами

## Ссылки
- docs/architecture/security/jwt-structure.md
- docs/architecture/security/rbac.md
- docs/architecture/api-specification/auth-service.yaml

## Приоритет
Высокий

## Метки
backend, security, jwt

## Сложность
Medium

## История изменений
| Версия | Дата | Автор | Описание |
|--------|------|-------|----------|
| 1.0 | 2026-06-14 | Системный аналитик | Создание задачи |
