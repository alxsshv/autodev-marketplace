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

## GitLab Issues

| Issue | Название | Статус | Приоритет | Оценка | Блокирует |
|-------|----------|--------|-----------|--------|-----------|
| TS-003-2.1 | Token Service (генерация JWT) | To Do | Normal | 12 часов | Sprint 3 |
| TS-003-2.2 | JwtParser (валидация JWT) | To Do | Normal | 10 часов | Sprint 3 |
| TS-003-2.3 | RedisTokenRepository (revoked tokens) | To Do | Normal | 8 часов | Sprint 3 |
| TS-003-2.4 | Token Pair DTO | To Do | Normal | 2 часа | Sprint 4 |
| TS-003-2.5 | Unit тесты JWT Token Service | To Do | Normal | 12 часов | Sprint 3 |

---

## Зависимости

**Этот спринт блокирует:**
- Sprint 3 (Spring Security Configuration)
- Sprint 4 (API endpoints)
- Sprint 5 (Keycloak Integration)
- Sprint 6 (Platform Service Integration)
- Sprint 7 (OAuth2 Provider Management)
- Sprint 8 (Rate Limiting)

**Зависит от:**
- Sprint 1 (Redis, PostgreSQL, Base models)

---

## Критерии готовности

- [ ] Генерация access token (15 мин TTL, RS256) работает
- [ ] Генерация refresh token (7 дней TTL, одноразовый) работает
- [ ] Валидация JWT с RS256 подписью работает
- [ ] Redis хранит revoked tokens (revoked:access, revoked:refresh, used_refresh)
- [ ] Покрытие unit тестов >80% для JWT Token Service
- [ ] Демонстрация: токены генерируются и валидируются в unit тестах
