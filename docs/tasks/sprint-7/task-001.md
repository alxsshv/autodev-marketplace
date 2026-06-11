# Sprint 7: OAuth2 Provider Management

**Эпик:** TS-003-SPRINT-7  
**Статус:** To Do  
**Приоритет:** Normal  
**Оценка времени:** 4 дня (32 часа)  
**Блокирует:** Нет (финальный спринт для MVP)  
**Зависимости:** Sprint 1 (Base models), Sprint 2 (JWT Token Service), Sprint 3 (Security Configuration), Sprint 4 (API endpoints), Sprint 5 (Keycloak Integration), Sprint 6 (Platform Service Integration)

---

## Описание

Управление OAuth2 провайдерами:
- Таблица `oauth_providers` в PostgreSQL
- Привязка/отвязка OAuth2 аккаунтов
- Синхронизация профиля из OAuth2 провайдера
- Unit тесты для всех операций

---

## Задачи

### 7.1. oauth_providers таблица

**Оценка:** 6 часов  
**Критерии приёмки:**
- AC-7.1: Миграция `03-06-2026-create-table-oauth-providers.sql` создает таблицу `auth.oauth_providers`
- AC-7.2: Поля таблицы: id, user_id, provider, provider_id, access_token, refresh_token, expires_at, scopes
- AC-7.3: Индексы: idx_oauth_providers_user, idx_oauth_providers_provider, idx_oauth_providers_provider_id
- AC-7.4: Unit тест `OAuthProviderRepositoryTest` проверяет CRUD операции

**Зависимости:** Sprint 1 (PostgreSQL, Base models)

---

### 7.2. OAuthProviderPort (интерфейс)

**Оценка:** 4 часа  
**Критерии приёмки:**
- AC-7.5: `OAuthProviderPort` интерфейс с методами: `linkProvider()`, `unlinkProvider()`, `getProviders()`, `syncProfile()`
- AC-7.6: `OAuthProviderRepository` Spring Data JPA репозиторий для работы с таблицей
- AC-7.7: Unit тест `OAuthProviderPortTest` проверяет создание клиента

**Зависимости:** Sprint 1 (Gradle проект), 7.1 (oauth_providers таблица)

---

### 7.3. Основные endpoints

**Оценка:** 12 часов  
**Критерии приёмки:**
- AC-7.8: `ExternalAuthController.linkProvider()` — `POST /api/v1/auth/external/link` привязка OAuth2 аккаунта
- AC-7.9: `ExternalAuthController.unlinkProvider()` — `POST /api/v1/auth/external/unlink` отвязка OAuth2 аккаунта
- AC-7.10: `ExternalAuthController.getProviders()` — `GET /api/v1/auth/external/providers` список привязанных провайдеров
- AC-7.11: Защита всех endpoints через `@PreAuthorize("isAuthenticated()")`
- AC-7.12: Unit тест `ExternalAuthControllerTest.linkProvider()` проверяет привязку провайдера

**Зависимости:** Sprint 2 (JWT Token Service), Sprint 3 (Security Configuration), Sprint 4 (API endpoints), 7.1 (oauth_providers таблица), 7.2 (OAuthProviderPort)

---

### 7.4. Синхронизация профиля

**Оценка:** 8 часов  
**Критерии приёмки:**
- AC-7.13: `OAuthProviderService.syncProfile()` синхронизирует данные из провайдера в `platform_service.user_profiles`
- AC-7.14: Обновление `store_name`, `avatar_url` из данных провайдера
- AC-7.15: Unit тест `OAuthProviderServiceTest.syncProfile()` проверяет синхронизацию профиля

**Зависимости:** Sprint 6 (Platform Service Integration), 7.2 (OAuthProviderPort)

---

## Критерии готовности спринта

- [ ] `POST /api/v1/auth/external/link` привязывает OAuth2 аккаунт
- [ ] `POST /api/v1/auth/external/unlink` отвязывает OAuth2 аккаунт (но не последний)
- [ ] `GET /api/v1/auth/external/providers` возвращает список привязанных провайдеров
- [ ] Синхронизация профиля из OAuth2 провайдера работает
- [ ] Демонстрация: привязка, отвязка, синхронизация профиля

---

## Зависимости от других спринтов

**Этот спринт не блокирует другие спринты** — финальный спринт для MVP.

**Зависит от:**
- Sprint 1 (Base models, PostgreSQL, Redis)
- Sprint 2 (JWT Token Service — проверка токена)
- Sprint 3 (Security Configuration — protected endpoints)
- Sprint 4 (API endpoints — base endpoints)
- Sprint 5 (Keycloak Integration — для базовой аутентификации)
- Sprint 6 (Platform Service Integration — синхронизация профиля)

---

## GitLab issue structure

```
TS-003-SPRINT-6: Sprint 6: Platform Service Integration (Epic)
├── TS-003-SPRINT-7: Sprint 7: OAuth2 Provider Management (Epic)
│   ├── TS-003-7.1: oauth_providers таблица (Issue)
│   ├── TS-003-7.2: OAuthProviderPort (интерфейс) (Issue)
│   ├── TS-003-7.3: Основные endpoints (Issue)
│   └── TS-003-7.4: Синхронизация профиля (Issue)
```

---

## Примечания

- Поддерживаемые провайдеры: keycloak, google, github (можно расширить)
- Отвязка последнего провайдера запрещена (требуется хотя бы один провайдер)
- Синхронизация профиля происходит при входе через OAuth2
- Access token и refresh token хранятся в PostgreSQL (шаблон для будущих расширений)
