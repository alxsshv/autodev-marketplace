# Sprint 3: Spring Security Configuration

**Эпик:** TS-003-SPRINT-3  
**Статус:** To Do  
**Приоритет:** Normal  
**Оценка времени:** 4 дня (32 часа)  
**Блокирует:** Sprint 4, Sprint 5, Sprint 6, Sprint 7, Sprint 8, Sprint 9  
**Зависимости:** Sprint 1 (Base models), Sprint 2 (JWT Token Service)

---

## Описание

Настройка Spring Security для Auth Service:
- JwtAuthenticationFilter для валидации JWT токенов
- SecurityConfig с @EnableMethodSecurity
- @PreAuthorize для защиты endpoints
- Unit тесты Security конфигурации

---

## Задачи

### 3.1. JwtAuthenticationFilter

**Оценка:** 10 часов  
**Критерии приёмки:**
- AC-3.1: `JwtAuthenticationFilter` извлекает токен из заголовка `Authorization: Bearer {token}`
- AC-3.2: `JwtAuthenticationFilter` вызывает `JwtParser` для валидации RS256 подписи и claims
- AC-3.3: `JwtAuthenticationFilter` создает `Authentication` объект с email (principal), ролями (authorities)
- AC-3.4: При невалидном токене возвращает `401 Unauthorized` с JSON ошибки
- AC-3.5: Unit тест `JwtAuthenticationFilterTest.filterValidToken()` проверяет валидацию токена через фильтр
- AC-3.6: Unit тест `JwtAuthenticationFilterTest.filterInvalidToken()` проверяет возврат 401 для невалидного токена

**Зависимости:** Sprint 1 (Gradle проект), Sprint 2 (JwtParser)

---

### 3.2. SecurityConfig

**Оценка:** 8 часов  
**Критерии приёмки:**
- AC-3.7: `SecurityConfig` содержит `@EnableMethodSecurity(prePostEnabled = true)` для @PreAuthorize
- AC-3.8: `JwtAuthenticationFilter` добавляется в цепочку фильтров перед `UsernamePasswordAuthenticationFilter`
- AC-3.9: Разрешены эндпоинты: `POST /api/v1/auth/login`, `POST /api/v1/auth/refresh`, `GET /api/v1/auth/validate-token`
- AC-3.10: Защищены endpoints: `GET /api/v1/auth/me`, `POST /api/v1/auth/logout`
- AC-3.11: `@PreAuthorize("hasRole('ADMIN')")` защищает `/api/v1/admin/**` endpoints
- AC-3.12: Unit тест `SecurityConfigTest.adminEndpointAccess()` проверка @PreAuthorize("hasRole('ADMIN')")

**Зависимости:** Sprint 1 (Base models), Sprint 2 (JwtParser, Token Service), 3.1 (JwtAuthenticationFilter)

---

### 3.3. Authentication объект

**Оценка:** 4 часа  
**Критерии приёмки:**
- AC-3.13: `Authentication.principal` содержит email пользователя (строка)
- AC-3.14: `Authentication.credentials` равен null (JWT уже валидирован)
- AC-3.15: `Authentication.authorities` содержит роли пользователя (BUYER, SELLER, MODERATOR, ADMIN)
- AC-3.16: Unit тест `AuthenticationTest` проверяет структуру Authentication объекта

**Зависимости:** Sprint 1 (Base models), Sprint 2 (JwtParser)

---

### 3.4. Security error responses

**Оценка:** 4 часа  
**Критерии приёмки:**
- AC-3.17: `401 Unauthorized` для невалидного/отозванного токена с JSON: `{"error": "InvalidToken", "message": "..."}`  
- AC-3.18: `403 Forbidden` для недостатка прав (не ADMIN для `/admin/**`)
- AC-3.19: `429 Too Many Requests` для превышения rate limiting (используется Redis из Sprint 1)
- AC-3.20: Unit тест `SecurityErrorTest.invalidToken()` проверяет возврат 401

**Зависимости:** Sprint 1 (Redis), Sprint 2 (JWT Token Service), 3.1 (JwtAuthenticationFilter)

---

### 3.5. Unit тесты Security Configuration

**Оценка:** 6 часов  
**Критерии приёмки:**
- AC-3.21: `JwtParserTest.parseValidToken()` — проверка парсинга JWT с RS256 подписью
- AC-3.22: `JwtParserTest.parseExpiredToken()` — проверка отказа от истёкшего токена
- AC-3.23: `JwtAuthenticationFilterTest.filterValidToken()` — проверка валидации токена через фильтр
- AC-3.24: `JwtAuthenticationFilterTest.filterInvalidToken()` — проверка возврата 401 для невалидного токена
- AC-3.25: `SecurityConfigTest.adminEndpointAccess()` — проверка `@PreAuthorize("hasRole('ADMIN')")`

**Зависимости:** 3.1 (JwtAuthenticationFilter), 3.2 (SecurityConfig)

---

## Критерии готовности спринта

- [ ] JwtAuthenticationFilter валидирует токены и создает Authentication объект
- [ ] SecurityConfig защищает endpoints через @PreAuthorize
- [ ] `401 Unauthorized` возвращается для невалидного токена
- [ ] `403 Forbidden` возвращается для недостатка прав
- [ ] Покрытие unit тестов >80% для Security конфигурации
- [ ] Демонстрация: filter проходит валидацию токена, защищенные endpoints возвращают 401/403

---

## Зависимости от других спринтов

**Этот спринт блокирует:**
- Sprint 4 (API endpoints) — JWT фильтр должен быть в цепочке для protected endpoints
- Sprint 5 (Keycloak Integration) — login endpoint требует Security конфигурацию
- Sprint 6 (Platform Service Integration) — protected endpoints требуют Security
- Sprint 7 (OAuth2 Provider Management) — protected endpoints требуют Security
- Sprint 8 (Rate Limiting) — rate limiting фильтры добавляются после Security
- Sprint 9 (Admin Console) — `/admin/**` endpoints требуют @PreAuthorize

**Зависит от:**
- Sprint 1 (Base models)
- Sprint 2 (JWT Token Service — JwtParser для валидации)

---

## GitLab issue structure

```
TS-003-SPRINT-2: Sprint 2: JWT Token Service (Epic)
├── TS-003-SPRINT-3: Sprint 3: Spring Security Configuration (Epic)
│   ├── TS-003-3.1: JwtAuthenticationFilter (Issue)
│   ├── TS-003-3.2: SecurityConfig (Issue)
│   ├── TS-003-3.3: Authentication объект (Issue)
│   ├── TS-003-3.4: Security error responses (Issue)
│   └── TS-003-3.5: Unit тесты Security Configuration (Issue)
```

---

## Примечания

- JwtAuthenticationFilter должен быть первым фильтром после SecurityContextPersistenceFilter
- @EnableMethodSecurity(prePostEnabled = true) разрешает использование @PreAuthorize на методах
- Все protected endpoints проверяют токен через JwtAuthenticationFilter перед выполнением
- Error responses должны быть стандартизированы через `@ControllerAdvice`
