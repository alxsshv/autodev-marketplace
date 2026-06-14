# Техническое задание: Интеграция с Redis для кэширования токенов и пользователей

## Название задачи
Интегрировать Redis (AUTH-009)

## Название ветки
feature/AUTH-009-integrate-redis

## Описание
Необходимо настроить Redis для кэширования токенов и профилей пользователей. Согласно архитектуре, revoked tokens хранятся только в Redis (Redis-only подход).

## Цель задачи
Настроить Redis кэширование для:
- Хранения revoked tokens (TTL = expires_at - current_time)
- Хранения профилей пользователей (TTL = 1 hour)
- Кэширования JWT токенов (TTL = 12 hours)

## Критерии выполнения

- [ ] Создан `RedisConfig` с настройками подключения к Redis

- [ ] Создан `TokenCacheService`:
  - Метод `saveRevokedToken(String tokenHash, LocalDateTime expiresAt)` - сохраняет revoked token в Redis
  - Метод `isTokenRevoked(String tokenHash)` - проверяет, revoked ли токен
  - TTL рассчитывается как `expires_at - current_time`

- [ ] Создан `UserCacheService`:
  - Метод `getUserById(String keycloakUserId)` - получает пользователя из кэша
  - Метод `saveUser(UserDto user)` - сохраняет пользователя в кэш
  - TTL = 1 hour

- [ ] Создан `TokenValidationService`:
  - Метод `isTokenValid(String token)` - проверяет валидность токена (не expired и не revoked)
  - Проверка через Redis (revoked tokens)
  - Проверка через JWTDecoder (expiration)

- [ ] Настроены Redis ключи:
  - `auth:blacklist:{token_hash}` - revoked token
  - `auth:token:{token_hash}` - valid token (опционально)
  - `auth:user:{keycloak_user_id}` - user profile

- [ ] Обработка ошибок Redis (fallback на JWTDecoder при падении Redis)

- [ ] Все сервисы покрыты модульными тестами

- [ ] Добавлены Javadoc комментарии для всех методов

## Ссылки
- docs/architecture/security/revoked-tokens.md
- docs/architecture/glossary.md - раздел "Redis ключи"

## Приоритет
Средний

## Метки
backend, redis, caching

## Сложность
Medium

## История изменений
| Версия | Дата | Автор | Описание |
|--------|------|-------|----------|
| 1.0 | 2026-06-14 | Системный аналитик | Создание задачи |
