# Sprint 2: JWT Token Service

**Эпик:** TS-003-SPRINT-2  
**Статус:** To Do  
**Приоритет:** Normal  
**Оценка времени:** 6 дней (48 часов)  
**Блокирует:** Sprint 3, Sprint 4, Sprint 5, Sprint 6, Sprint 7, Sprint 8  
**Зависимости:** Sprint 1 (Redis, PostgreSQL, Base models)

---

## Описание

Создание JWT Token Service — центрального компонента Auth Service:
- Генерация JWT токенов (RS256 алгоритм)
- Валидация JWT токенов (проверка подписи, claims, TTL)
- Redis репозиторий для revoked tokens
- Unit тесты для всех компонентов

---

## Задачи

### 2.1. Token Service (генерация JWT)

**Оценка:** 12 часов  
**Критерии приёмки:**
- AC-2.1: `TokenService.generateAccessToken()` генерирует JWT с RS256, 15 мин TTL
- AC-2.2: `TokenService.generateRefreshToken()` генерирует JWT с RS256, 7 дней TTL, уникальный jti
- AC-2.3: Access token содержит обязательные claims: sub, roles, iat, exp, aud, iss
- AC-2.4: Refresh token содержит обязательные claims: sub, jti, iat, exp, aud, iss
- AC-2.5: Unit тест `TokenServiceTest.generateAccessToken()` проверяет структуру токена
- AC-2.6: Unit тест `TokenServiceTest.generateRefreshToken()` проверяет jti уникальность

**Зависимости:** Sprint 1 (Gradle проект, dependencies)

---

### 2.2. JwtParser (валидация JWT)

**Оценка:** 10 часов  
**Критерии приёмки:**
- AC-2.7: `JwtParser.parseToken()` валидирует RS256 подпись через публичный ключ Keycloak
- AC-2.8: Проверка обязательных claims: exp (не истёк), iat (в прошлом), roles (массив), aud, iss
- AC-2.9: Кэширование публичного ключа Keycloak в Redis (TTL: 1 час)
- AC-2.10: Unit тест `JwtParserTest.parseValidToken()` проверяет валидацию успешного токена
- AC-2.11: Unit тест `JwtParserTest.parseExpiredToken()` проверяет отказ от истёкшего токена

**Зависимости:** Sprint 1 (Redis конфигурация), 2.1 (Token Service)

---

### 2.3. RedisTokenRepository (revoked tokens)

**Оценка:** 8 часов  
**Критерии приёмки:**
- AC-2.12: `RedisTokenRepository.saveRevokedAccessToken()` добавляет `revoked:access:{hash}` (15 мин TTL)
- AC-2.13: `RedisTokenRepository.saveRevokedRefreshToken()` добавляет `revoked:refresh:{hash}` (7 дней TTL)
- AC-2.14: `RedisTokenRepository.isAccessTokenRevoked()` проверяет наличие в Redis
- AC-2.15: `RedisTokenRepository.saveUsedRefreshToken()` использует SETNX для `used_refresh:{jti}` (7 дней TTL)
- AC-2.16: Unit тест `RedisTokenRepositoryTest.saveRevokedToken()` проверяет Redis структуру

**Зависимости:** Sprint 1 (Redis конфигурация), 2.1 (Token Service)

---

### 2.4. Token Pair DTO

**Оценка:** 2 часа  
**Критерии приёмки:**
- AC-2.17: `TokenPair` DTO содержит: access_token (JWT), refresh_token (JWT), token_type (Bearer), expires_in
- AC-2.18: Unit тест `TokenPairTest` проверает serialization/deserialization

**Зависимости:** Sprint 1 (DTO модели)

---

### 2.5. Unit тесты JWT Token Service

**Оценка:** 12 часов  
**Критерии приёмки:**
- AC-2.19: `TokenServiceTest.generateAccessToken()` — проверка генерации access token (15 мин TTL, RS256)
- AC-2.20: `TokenServiceTest.generateRefreshToken()` — проверка генерации refresh token (7 дней TTL, одноразовый)
- AC-2.21: `TokenServiceTest.validateAccessToken()` — проверка валидации с обязательными claims
- AC-2.22: `TokenServiceTest.refreshTokens()` — проверка обновления токенов и инвалидации старого refresh token
- AC-2.23: `TokenServiceTest.revokeTokens()` — проверка добавления в Redis revoked tokens
- AC-2.24: `RedisTokenRepositoryTest.checkRevokedToken()` — проверка проверки revoked токена через Redis
- AC-2.25: `RedisTokenRepositoryTest.cacheRevokedCheck()` — проверка кэширования результата проверки revoked (TTL: 1 мин)
- AC-2.26: `JwtParserTest.cachePublicKey()` — проверка кэширования публичного ключа Keycloak (TTL: 1 час)

**Зависимости:** 2.1 (Token Service), 2.2 (JwtParser), 2.3 (RedisTokenRepository)

---

## Критерии готовности спринта

- [ ] Генерация access token (15 мин TTL, RS256) работает
- [ ] Генерация refresh token (7 дней TTL, одноразовый) работает
- [ ] Валидация JWT с RS256 подписью работает
- [ ] Redis хранит revoked tokens (revoked:access, revoked:refresh, used_refresh)
- [ ] Покрытие unit тестов >80% для JWT Token Service
- [ ] Демонстрация: токены генерируются и валидируются в unit тестах

---

## Зависимости от других спринтов

**Этот спринт блокирует:**
- Sprint 3 (Spring Security) — JWT Token Service критичен для JwtAuthenticationFilter
- Sprint 4 (API endpoints) — login, refresh, logout требуют Token Service
- Sprint 5 (Keycloak Integration) — login через Keycloak требует Token Service
- Sprint 6 (Platform Service Integration) — вход пользователя требует Token Service
- Sprint 7 (OAuth2 Provider Management) — вход через OAuth2 требует Token Service
- Sprint 8 (Rate Limiting) — использует Redis, уже готов (Sprint 1)

**Зависит от:**
- Sprint 1 (Redis, PostgreSQL, Base models)

---

## GitLab issue structure

```
TS-003-SPRINT-1: Sprint 1: Базовая инфраструктура (Epic)
├── TS-003-SPRINT-2: Sprint 2: JWT Token Service (Epic)
│   ├── TS-003-2.1: Token Service (генерация JWT) (Issue)
│   ├── TS-003-2.2: JwtParser (валидация JWT) (Issue)
│   ├── TS-003-2.3: RedisTokenRepository (revoked tokens) (Issue)
│   ├── TS-003-2.4: Token Pair DTO (Issue)
│   └── TS-003-2.5: Unit тесты JWT Token Service (Issue)
```

---

## Примечания

- Все JWT токены используют RS256 алгоритм (не HS256)
- Refresh token одноразовый — после использования помечается как `used_refresh:{jti}`
- Кэширование публичного ключа Keycloak TTL: 1 час, кэш результатов проверки revoked TTL: 1 мин
- При недоступности Keycloak возвращается 503 Service Unavailable, если кэш пуст
