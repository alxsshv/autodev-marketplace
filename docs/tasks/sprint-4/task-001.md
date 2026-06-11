# Sprint 4: Основные API endpoints

**Эпик:** TS-003-SPRINT-4  
**Статус:** To Do  
**Приоритет:** Normal  
**Оценка времени:** 5 дней (40 часов)  
**Блокирует:** Sprint 5, Sprint 6, Sprint 7  
**Зависимости:** Sprint 1 (Base models), Sprint 2 (JWT Token Service), Sprint 3 (Security Configuration)

---

## Описание

Реализация основных API endpoints для аутентификации:
- POST /api/v1/auth/login — вход пользователя
- POST /api/v1/auth/refresh — обновление токенов
- POST /api/v1/auth/logout — выход пользователя
- GET /api/v1/auth/me — информация о текущем пользователе
- GET /api/v1/auth/validate-token — проверка токена (для API Gateway)

---

## Задачи

### 4.1. POST /api/v1/auth/login

**Оценка:** 12 часов  
**Критерии приёмки:**
- AC-4.1: `AuthController.login()` принимает `LoginRequest` (username, password)
- AC-4.2: Валидация через Keycloak OIDC (grant_type=password или service account)
- AC-4.3: Генерация пары токенов через TokenService (access_token 15 мин, refresh_token 7 дней)
- AC-4.4: Возврат `TokenPair` DTO с access_token, refresh_token, token_type (Bearer), expires_in
- AC-4.5: `401 Unauthorized` при неверных учётных данных
- AC-4.6: `429 Too Many Requests` при превышении лимита (5 попыток за 5 минут — Redis из Sprint 1)
- AC-4.7: Unit тест `AuthControllerTest.loginSuccess()` проверяет успешный вход
- AC-4.8: Unit тест `AuthControllerTest.loginInvalidCredentials()` проверяет 401 для неверных данных

**Зависимости:** Sprint 1 (Redis для rate limiting), Sprint 2 (Token Service), Sprint 3 (Security Configuration)

---

### 4.2. POST /api/v1/auth/refresh

**Оценка:** 8 часов  
**Критерии приёмки:**
- AC-4.9: `AuthController.refresh()` принимает `RefreshRequest` (refresh_token)
- AC-4.10: Проверка refresh_token через JwtParser (RS256, exp, jti)
- AC-4.11: Проверка Redis на `used_refresh:{jti}` — если есть — возврат `409 Conflict`
- AC-4.12: Инвалидация старого refresh token (добавление в Redis `used_refresh:{jti}`)
- AC-4.13: Генерация новой пары токенов через TokenService
- AC-4.14: Возврат новой пары токенов в `TokenPair` DTO
- AC-4.15: Unit тест `AuthControllerTest.refreshSuccess()` проверяет успешное обновление
- AC-4.16: Unit тест `AuthControllerTest.refreshUsedToken()` проверяет 409 для использованного refresh token

**Зависимости:** Sprint 1 (Redis для used_refresh tokens), Sprint 2 (Token Service, RedisTokenRepository), Sprint 3 (Security Configuration)

---

### 4.3. POST /api/v1/auth/logout

**Оценка:** 6 часов  
**Критерии приёмки:**
- AC-4.17: `AuthController.logout()` извлекает токен из заголовка `Authorization: Bearer {token}`
- AC-4.18: Инвалидация access_token (добавление `revoked:access:{hash}` в Redis, TTL 15 мин)
- AC-4.19: Инвалидация refresh_token (добавление `revoked:refresh:{hash}` в Redis, TTL 7 дней)
- AC-4.20: Удаление `used_refresh:{jti}` из Redis (если был)
- AC-4.21: Unit тест `AuthControllerTest.logoutSuccess()` проверяет успешный выход

**Зависимости:** Sprint 1 (Redis для revoked tokens), Sprint 2 (Token Service, RedisTokenRepository), Sprint 3 (Security Configuration)

---

### 4.4. GET /api/v1/auth/me

**Оценка:** 6 часов  
**Критерии приёмки:**
- AC-4.22: `AuthController.me()` возвращает информацию о текущем пользователе
- AC-4.23: Возврат DTO с id, email, roles, platform_profile (store_name, verification_status, loyalty_balance)
- AC-4.24: `401 Unauthorized` для невалидного/отозванного токена
- AC-4.25: Unit тест `AuthControllerTest.meSuccess()` проверяет успешный запрос

**Зависимости:** Sprint 2 (Token Service для проверки токена), Sprint 3 (Security Configuration)

---

### 4.5. GET /api/v1/auth/validate-token

**Оценка:** 4 часа  
**Критерии приёмки:**
- AC-4.26: `AuthController.validateToken()` вызывается API Gateway
- AC-4.27: Валидация токена через JwtParser (RS256, claims, TTL)
- AC-4.28: Проверка Redis на `revoked:access:{hash}` — если отозван — `401 Unauthorized`
- AC-4.29: Возврат `200 OK` для валидного токена
- AC-4.30: Unit тест `AuthControllerTest.validateTokenSuccess()` проверяет валидный токен

**Зависимости:** Sprint 1 (Redis для revoked tokens), Sprint 2 (Token Service, RedisTokenRepository), Sprint 3 (Security Configuration)

---

### 4.6. Unit тесты API endpoints

**Оценка:** 4 часа  
**Критерии приёмки:**
- AC-4.31: `AuthControllerTest.loginSuccess()` — проверка успешного входа
- AC-4.32: `AuthControllerTest.loginInvalidCredentials()` — проверка 401 для неверных данных
- AC-4.33: `AuthControllerTest.refreshSuccess()` — проверка успешного обновления
- AC-4.34: `AuthControllerTest.refreshUsedToken()` — проверка 409 для использованного refresh token
- AC-4.35: `AuthControllerTest.logoutSuccess()` — проверка успешного выхода
- AC-4.36: `AuthControllerTest.meSuccess()` — проверка успешного запроса профиля
- AC-4.37: `AuthControllerTest.validateTokenSuccess()` — проверка валидного токена

**Зависимости:** 4.1 (POST /api/v1/auth/login), 4.2 (POST /api/v1/auth/refresh), 4.3 (POST /api/v1/auth/logout), 4.4 (GET /api/v1/auth/me), 4.5 (GET /api/v1/auth/validate-token)

---

## Критерии готовности спринта

- [ ] `POST /api/v1/auth/login` работает и возвращает пару токенов
- [ ] `POST /api/v1/auth/refresh` инвалидирует старый refresh token и генерирует новую пару
- [ ] `POST /api/v1/auth/logout` помечает токены как отозванные в Redis
- [ ] `GET /api/v1/auth/me` возвращает данные пользователя
- [ ] `GET /api/v1/auth/validate-token` проверяет токен и возвращает статус
- [ ] Покрытие unit тестов >80% для API endpoints
- [ ] Демонстрация: все endpoints работают через curl или Postman

---

## Зависимости от других спринтов

**Этот спринт блокирует:**
- Sprint 5 (Keycloak Integration) — login endpoint требует Keycloak OIDC
- Sprint 6 (Platform Service Integration) — me endpoint требует синхронизацию профиля
- Sprint 7 (OAuth2 Provider Management) — вход через OAuth2 требует Token Service

**Зависит от:**
- Sprint 1 (Redis для rate limiting, revoked tokens)
- Sprint 2 (JWT Token Service — генерация и валидация токенов)
- Sprint 3 (Security Configuration — защита endpoints, JWT фильтр)

---

## GitLab issue structure

```
TS-003-SPRINT-3: Sprint 3: Spring Security Configuration (Epic)
├── TS-003-SPRINT-4: Sprint 4: Основные API endpoints (Epic)
│   ├── TS-003-4.1: POST /api/v1/auth/login (Issue)
│   ├── TS-003-4.2: POST /api/v1/auth/refresh (Issue)
│   ├── TS-003-4.3: POST /api/v1/auth/logout (Issue)
│   ├── TS-003-4.4: GET /api/v1/auth/me (Issue)
│   ├── TS-003-4.5: GET /api/v1/auth/validate-token (Issue)
│   └── TS-003-4.6: Unit тесты API endpoints (Issue)
```

---

## Примечания

- Все endpoints защищены Security Configuration через JWT фильтр
- Rate limiting для login (5/5min) реализуется через Redis из Sprint 1
- Revoked tokens проверяются через Redis из Sprint 2 (revoked:access, revoked:refresh)
- Validate-token endpoint вызывается API Gateway для проверки каждого запроса
