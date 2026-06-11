# Sprint 6: Platform Service Integration

**Эпик:** TS-003-SPRINT-6  
**Статус:** To Do  
**Приоритет:** Normal  
**Оценка времени:** 4 дня (32 часа)  
**Блокирует:** Sprint 7  
**Зависимости:** Sprint 1 (Base models), Sprint 2 (JWT Token Service), Sprint 3 (Security Configuration), Sprint 4 (API endpoints), Sprint 5 (Keycloak Integration)

---

## Описание

Интеграция с Platform Service для синхронизации профилей пользователей:
- Синхронизация `auth.users` ↔ `platform_service.user_profiles`
- Kafka producer событий (auth.user_authenticated, auth.user_logout, platform.user_registered, platform.user_verified)
- Unit тесты интеграции с Platform Service API

---

## Задачи

### 6.1. PlatformServicePort (интерфейс)

**Оценка:** 6 часов  
**Критерии приёмки:**
- AC-6.1: `PlatformServicePort` интерфейс с методами: `syncProfile()`, `getUserProfile()`, `updateVerificationStatus()`
- AC-6.2: `PlatformServiceClient` реализует `PlatformServicePort` через HTTP client (RestTemplate/WebClient)
- AC-6.3: Unit тест `PlatformServicePortTest` проверяет создание клиента

**Зависимости:** Sprint 1 (Gradle проект), Sprint 2 (JWT Token Service)

---

### 6.2. Синхронизация профиля пользователя

**Оценка:** 10 часов  
**Критерии приёмки:**
- AC-6.4: `PlatformSyncService.syncProfileOnLogin()` синхронизирует профиль при входе пользователя
- AC-6.5: `PlatformSyncService.syncProfileOnUpdate()` синхронизирует профиль при обновлении данных
- AC-6.6: Связь `auth.users.keycloak_user_id` ↔ `platform_service.user_profiles.keycloak_user_id`
- AC-6.7: Unit тест `PlatformSyncServiceTest.syncProfileOnLogin()` проверяет синхронизацию при входе

**Зависимости:** Sprint 1 (PostgreSQL, Base models), Sprint 2 (JWT Token Service), Sprint 6.1 (PlatformServicePort)

---

### 6.3. GET /api/v1/auth/me с профилем

**Оценка:** 6 часов  
**Критерии приёмки:**
- AC-6.8: `AuthController.me()` объединяет данные из `auth.users` и `platform_service.user_profiles`
- AC-6.9: Возврат `platform_profile` с полями: store_name, verification_status, loyalty_balance
- AC-6.10: Unit тест `AuthControllerTest.meWithProfile()` проверяет объединение данных

**Зависимости:** Sprint 1 (Base models), Sprint 2 (JWT Token Service), Sprint 4 (GET /api/v1/auth/me), Sprint 6.2 (Синхронизация профиля)

---

### 6.4. Kafka producer событий

**Оценка:** 8 часов  
**Критерии приёмки:**
- AC-6.11: `AuthEventProducer.publishAuthenticatedEvent()` публикует `auth.user_authenticated` при входе
- AC-6.12: `AuthEventProducer.publishLogoutEvent()` публикует `auth.user_logout` при выходе
- AC-6.13: `PlatformEventProducer.publishUserRegistered()` публикует `platform.user_registered` при регистрации
- AC-6.14: `PlatformEventProducer.publishUserVerified()` публикует `platform.user_verified` при верификации
- AC-6.15: При ошибке публикации — логировать и не прерывать выполнение (fire-and-forget)
- AC-6.16: Unit тест `AuthEventProducerTest.publishAuthenticatedEvent()` проверяет публикацию события

**Зависимости:** Sprint 1 (Gradle проект с Kafka dependency), Sprint 4 (API endpoints — login, logout)

---

## Критерии готовности спринта

- [ ] Синхронизация `auth.users` ↔ `platform_service.user_profiles` работает
- [ ] `GET /api/v1/auth/me` возвращает данные из обоих сервисов
- [ ] Kafka producer публикует события (auth.*, platform.*)
- [ ] При ошибке интеграции — выполнение продолжается (не прерывается)
- [ ] Демонстрация: профиль синхронизирован, события публикуются

---

## Зависимости от других спринтов

**Этот спринт блокирует:**
- Sprint 7 (OAuth2 Provider Management) — вход через OAuth2 требует синхронизации профиля

**Зависит от:**
- Sprint 1 (Base models, PostgreSQL)
- Sprint 2 (JWT Token Service — проверка токена)
- Sprint 3 (Security Configuration — protected endpoints)
- Sprint 4 (API endpoints — login, refresh, logout, me)
- Sprint 5 (Keycloak Integration — вход пользователя)

---

## GitLab issue structure

```
TS-003-SPRINT-5: Sprint 5: Keycloak Integration (Epic)
├── TS-003-SPRINT-6: Sprint 6: Platform Service Integration (Epic)
│   ├── TS-003-6.1: PlatformServicePort (интерфейс) (Issue)
│   ├── TS-003-6.2: Синхронизация профиля пользователя (Issue)
│   ├── TS-003-6.3: GET /api/v1/auth/me с профилем (Issue)
│   └── TS-003-6.4: Kafka producer событий (Issue)
```

---

## Примечания

- Platform Service API должен быть доступен по URL из конфигурации
- Kafka producer использует fire-and-forget стратегию (не блокирует выполнение при ошибках)
- Синхронизация профиля происходит только при входе и при явном обновлении данных
- Кэш профиля в Redis (TTL: 1 час) может быть добавлен в будущем для производительности
