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

## GitLab Issues

| Issue | Название | Статус | Приоритет | Оценка | Блокирует |
|-------|----------|--------|-----------|--------|-----------|
| TS-003-5.1 | Keycloak Admin Client | To Do | Normal | 12 часов | Sprint 6 |
| TS-003-5.2 | OIDC Login через Keycloak | To Do | Normal | 10 часов | Sprint 6 |
| TS-003-5.3 | Admin API endpoints | To Do | Normal | 8 часов | Sprint 9 |
| TS-003-5.4 | Integration тесты Keycloak | To Do | Normal | 8 часов | Sprint 6 |
| TS-003-5.5 | Keycloak configuration | To Do | Normal | 2 часа | Sprint 6 |

---

## Зависимости

**Этот спринт блокирует:**
- Sprint 6 (Platform Service Integration)
- Sprint 7 (OAuth2 Provider Management)

**Зависит от:**
- Sprint 1 (Base models, Redis)
- Sprint 2 (JWT Token Service — генерация токенов после входа)
- Sprint 3 (Security Configuration — JWT фильтр)
- Sprint 4 (API endpoints — login, refresh, logout, me, validate-token)

---

## Критерии готовности

- [ ] Keycloak OIDC login работает (grant_type=password)
- [ ] Keycloak Admin API для создания/списка/обновления ролей работает
- [ ] `@PreAuthorize("hasRole('ADMIN')")` защищает `/admin/**` endpoints
- [ ] Integration тесты проходят (Testcontainers Keycloak)
- [ ] Демонстрация: вход через Keycloak, управление пользователями через Admin API
