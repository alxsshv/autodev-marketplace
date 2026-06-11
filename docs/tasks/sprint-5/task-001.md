# Sprint 5: Keycloak Integration

**Эпик:** TS-003-SPRINT-5  
**Статус:** To Do  
**Приоритет:** Normal  
**Оценка времени:** 5 дней (40 часов)  
**Блокирует:** Sprint 6, Sprint 7  
**Зависимости:** Sprint 1 (Base models), Sprint 2 (JWT Token Service), Sprint 3 (Security Configuration), Sprint 4 (API endpoints)

---

## Описание

Интеграция с Keycloak для аутентификации и управления пользователями:
- Keycloak OIDC для входа пользователя (grant_type=password)
- Keycloak Admin API для управления пользователями (создание, обновление ролей)
- Unit тесты интеграции через Testcontainers Keycloak

---

## Задачи

### 5.1. Keycloak Admin Client

**Оценка:** 12 часов  
**Критерии приёмки:**
- AC-5.1: `KeycloakAdminClient` конфигурируется через `KeycloakClientProperties` (server-url, realm, client-id, credentials)
- AC-5.2: `KeycloakAdminClientAdapter` реализует `KeycloakPort` интерфейс
- AC-5.3: `KeycloakAdminClientAdapter.createUser()` создает пользователя в Keycloak через Admin API
- AC-5.4: `KeycloakAdminClientAdapter.getUsers()` получает список пользователей через Admin API
- AC-5.5: `KeycloakAdminClientAdapter.updateUserRoles()` обновляет роли пользователя через Admin API
- AC-5.6: Unit тест `KeycloakAdminClientAdapterTest.createUser()` проверяет создание пользователя (Testcontainers Keycloak)

**Зависимости:** Sprint 1 (Gradle проект с Keycloak Admin Client dependency), Sprint 3 (Security Configuration)

---

### 5.2. OIDC Login через Keycloak

**Оценка:** 10 часов  
**Критерии приёмки:**
- AC-5.7: `LoginUseCase` вызывает Keycloak OIDC endpoint `/{realm}/protocol/openid-connect/token` с grant_type=password
- AC-5.8: `LoginUseCase` валидирует ответ Keycloak и создает JWT токены через TokenService
- AC-5.9: Unit тест `LoginUseCaseTest.loginSuccess()` проверяет успешный вход через Keycloak OIDC
- AC-5.10: Unit тест `LoginUseCaseTest.loginInvalidCredentials()` проверяет обработку неверных учетных данных

**Зависимости:** Sprint 2 (Token Service), Sprint 4 (POST /api/v1/auth/login endpoint)

---

### 5.3. Admin API endpoints

**Оценка:** 8 часов  
**Критерии приёмки:**
- AC-5.11: `AdminController.getUsers()` возвращает список всех пользователей через Keycloak Admin API
- AC-5.12: `AdminController.createUser()` создает пользователя в Keycloak через Admin API (email + password)
- AC-5.13: `AdminController.updateUserRoles()` обновляет роли пользователя через Admin API
- AC-5.14: Все endpoints защищены `@PreAuthorize("hasRole('ADMIN')")`
- AC-5.15: Unit тест `AdminControllerTest.getUsers()` проверяет список пользователей

**Зависимости:** Sprint 1 (Base models), Sprint 3 (Security Configuration), Sprint 5.1 (Keycloak Admin Client)

---

### 5.4. Integration тесты Keycloak

**Оценка:** 8 часов  
**Критерии приёмки:**
- AC-5.16: `KeycloakIntegrationTest.loginSuccess()` — вход через Keycloak OIDC (Testcontainers Keycloak)
- AC-5.17: `KeycloakIntegrationTest.createUser()` — создание пользователя через Admin API
- AC-5.18: `KeycloakIntegrationTest.getUsers()` — получение списка пользователей
- AC-5.19: `KeycloakIntegrationTest.updateUserRoles()` — обновление ролей пользователя
- AC-5.20: `KeycloakIntegrationTest.adminProtected()` — проверка `@PreAuthorize("hasRole('ADMIN')")`

**Зависимости:** 5.1 (Keycloak Admin Client), 5.2 (OIDC Login), 5.3 (Admin API endpoints)

---

### 5.5. Keycloak configuration

**Оценка:** 2 часа  
**Критерии приёмки:**
- AC-5.21: `KeycloakClientProperties` содержит: server-url, realm, client-id, client-secret
- AC-5.22: Конфигурация подключения к уже настроенному Keycloak (realm=autodev, client=autodev-gateway)
- AC-5.23: Unit тест `KeycloakClientPropertiesTest` проверяет загрузку конфигурации

**Зависимости:** Sprint 1 (Gradle проект)

---

## Критерии готовности спринта

- [ ] Keycloak OIDC login работает (grant_type=password)
- [ ] Keycloak Admin API для создания/списка/обновления ролей работает
- [ ] `@PreAuthorize("hasRole('ADMIN')")` защищает `/admin/**` endpoints
- [ ] Integration тесты проходят (Testcontainers Keycloak)
- [ ] Демонстрация: вход через Keycloak, управление пользователями через Admin API

---

## Зависимости от других спринтов

**Этот спринт блокирует:**
- Sprint 6 (Platform Service Integration) — синхронизация пользователей требует Keycloak
- Sprint 7 (OAuth2 Provider Management) — вход через OAuth2 требует Token Service (уже готов)

**Зависит от:**
- Sprint 1 (Base models, Redis)
- Sprint 2 (JWT Token Service — генерация токенов после входа)
- Sprint 3 (Security Configuration — JWT фильтр)
- Sprint 4 (API endpoints — login, refresh, logout, me, validate-token)

---

## GitLab issue structure

```
TS-003-SPRINT-4: Sprint 4: Основные API endpoints (Epic)
├── TS-003-SPRINT-5: Sprint 5: Keycloak Integration (Epic)
│   ├── TS-003-5.1: Keycloak Admin Client (Issue)
│   ├── TS-003-5.2: OIDC Login через Keycloak (Issue)
│   ├── TS-003-5.3: Admin API endpoints (Issue)
│   ├── TS-003-5.4: Integration тесты Keycloak (Issue)
│   └── TS-003-5.5: Keycloak configuration (Issue)
```

---

## Примечания

- Keycloak уже настроен (realm=autodev, client=autodev-gateway) — только подключение
- OIDC login использует grant_type=password для MVP (в будущем можно перейти на authorization_code)
- Admin API требует service account credentials для вызовов
- Integration тесты через Testcontainers Keycloak позволяют тестировать без внешних зависимостей
