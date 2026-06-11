# GitLab Issues Plan for Auth Service (TS-003)

**Project:** Autodev Marketplace  
**URL:** https://alxsshv.com/Alxsshv/autodev-marketplace  
**Milestone:** MVP (due: 2026-07-15)

---

## Summary

- **Total Issues:** 10 Epic + Subtasks
- **Epic Issues:** 10 (по спринтам)
- **Subtasks:** ~30 (по задачам в каждом спринте)
- **Labels:** sprint-1 through sprint-10

---

## Structure

```
Autodev Marketplace (Project)
├── Milestone: MVP
├── Labels: sprint-1, sprint-2, ..., sprint-10
│
├── Issue #1: Sprint 1: Базовая инфраструктура (Epic)
│   ├── Subtasks: 6 задач
│
├── Issue #2: Sprint 2: JWT Token Service (Epic)
│   ├── Subtasks: 5 задач
│
├── Issue #3: Sprint 3: Spring Security Configuration (Epic)
│   ├── Subtasks: 5 задач
│
├── Issue #4: Sprint 4: Основные API endpoints (Epic)
│   ├── Subtasks: 6 задач
│
├── Issue #5: Sprint 5: Keycloak Integration (Epic)
│   ├── Subtasks: 5 задач
│
├── Issue #6: Sprint 6: Platform Service Integration (Epic)
│   ├── Subtasks: 4 задачи
│
├── Issue #7: Sprint 7: OAuth2 Provider Management (Epic)
│   ├── Subtasks: 4 задачи
│
├── Issue #8: Sprint 8: Rate Limiting (Epic)
│   ├── Subtasks: 3 задачи
│
├── Issue #9: Sprint 9: Admin Console (Epic)
│   ├── Subtasks: 2 задачи
│
└── Issue #10: Sprint 10: Аудит, логирование, финальные доработки (Epic)
    ├── Subtasks: 5 задач
```

---

## Epic Issues

| Issue | Title | Label | Status |
|-------|-------|-------|--------|
| #1 | Sprint 1: Базовая инфраструктура | sprint-1 | To Do |
| #2 | Sprint 2: JWT Token Service | sprint-2 | To Do |
| #3 | Sprint 3: Spring Security Configuration | sprint-3 | To Do |
| #4 | Sprint 4: Основные API endpoints | sprint-4 | To Do |
| #5 | Sprint 5: Keycloak Integration | sprint-5 | To Do |
| #6 | Sprint 6: Platform Service Integration | sprint-6 | To Do |
| #7 | Sprint 7: OAuth2 Provider Management | sprint-7 | To Do |
| #8 | Sprint 8: Rate Limiting | sprint-8 | To Do |
| #9 | Sprint 9: Admin Console | sprint-9 | To Do |
| #10 | Sprint 10: Аудит, логирование, финальные доработки | sprint-10 | To Do |

---

## Subtasks per Sprint

### Sprint 1: Базовая инфраструктура (6 subtasks)

| Issue | Title | Label | Due Date |
|-------|-------|-------|----------|
| 1.1 | Sprint 1.1: Создание Gradle проекта | sprint-1, backend | 2026-06-12 |
| 1.2 | Sprint 1.2: PostgreSQL миграции (users, roles, indexes) | sprint-1, database | 2026-06-13 |
| 1.3 | Sprint 1.3: Redis конфигурация | sprint-1, redis | 2026-06-12 |
| 1.4 | Sprint 1.4: Entity модели | sprint-1, backend | 2026-06-13 |
| 1.5 | Sprint 1.5: DTO модели | sprint-1, backend | 2026-06-12 |
| 1.6 | Sprint 1.6: Integration тесты | sprint-1, testing | 2026-06-14 |

### Sprint 2: JWT Token Service (5 subtasks)

| Issue | Title | Label | Due Date |
|-------|-------|-------|----------|
| 2.1 | Sprint 2.1: Token Service (генерация JWT) | sprint-2, backend | 2026-06-18 |
| 2.2 | Sprint 2.2: JwtParser (валидация JWT) | sprint-2, security | 2026-06-18 |
| 2.3 | Sprint 2.3: RedisTokenRepository (revoked tokens) | sprint-2, redis | 2026-06-17 |
| 2.4 | Sprint 2.4: Token Pair DTO | sprint-2, backend | 2026-06-14 |
| 2.5 | Sprint 2.5: Unit тесты JWT Token Service | sprint-2, testing | 2026-06-19 |

### Sprint 3: Spring Security Configuration (5 subtasks)

| Issue | Title | Label | Due Date |
|-------|-------|-------|----------|
| 3.1 | Sprint 3.1: JwtAuthenticationFilter | sprint-3, security | 2026-06-20 |
| 3.2 | Sprint 3.2: SecurityConfig | sprint-3, security | 2026-06-20 |
| 3.3 | Sprint 3.3: Authentication объект | sprint-3, backend | 2026-06-19 |
| 3.4 | Sprint 3.4: Security error responses | sprint-3, security | 2026-06-20 |
| 3.5 | Sprint 3.5: Unit тесты Security Configuration | sprint-3, testing | 2026-06-21 |

### Sprint 4: Основные API endpoints (6 subtasks)

| Issue | Title | Label | Due Date |
|-------|-------|-------|----------|
| 4.1 | Sprint 4.1: POST /api/v1/auth/login | sprint-4, backend | 2026-06-24 |
| 4.2 | Sprint 4.2: POST /api/v1/auth/refresh | sprint-4, backend | 2026-06-24 |
| 4.3 | Sprint 4.3: POST /api/v1/auth/logout | sprint-4, backend | 2026-06-23 |
| 4.4 | Sprint 4.4: GET /api/v1/auth/me | sprint-4, backend | 2026-06-23 |
| 4.5 | Sprint 4.5: GET /api/v1/auth/validate-token | sprint-4, backend | 2026-06-22 |
| 4.6 | Sprint 4.6: Unit тесты API endpoints | sprint-4, testing | 2026-06-25 |

### Sprint 5: Keycloak Integration (5 subtasks)

| Issue | Title | Label | Due Date |
|-------|-------|-------|----------|
| 5.1 | Sprint 5.1: Keycloak Admin Client | sprint-5, backend | 2026-06-26 |
| 5.2 | Sprint 5.2: OIDC Login через Keycloak | sprint-5, backend | 2026-06-26 |
| 5.3 | Sprint 5.3: Admin API endpoints | sprint-5, backend | 2026-06-25 |
| 5.4 | Sprint 5.4: Integration тесты Keycloak | sprint-5, testing | 2026-06-27 |
| 5.5 | Sprint 5.5: Keycloak configuration | sprint-5, backend | 2026-06-24 |

### Sprint 6: Platform Service Integration (4 subtasks)

| Issue | Title | Label | Due Date |
|-------|-------|-------|----------|
| 6.1 | Sprint 6.1: PlatformServicePort (интерфейс) | sprint-6, backend | 2026-06-28 |
| 6.2 | Sprint 6.2: Синхронизация профиля пользователя | sprint-6, backend | 2026-06-28 |
| 6.3 | Sprint 6.3: GET /api/v1/auth/me с профилем | sprint-6, backend | 2026-06-27 |
| 6.4 | Sprint 6.4: Kafka producer событий | sprint-6, kafka | 2026-06-29 |

### Sprint 7: OAuth2 Provider Management (4 subtasks)

| Issue | Title | Label | Due Date |
|-------|-------|-------|----------|
| 7.1 | Sprint 7.1: oauth_providers таблица | sprint-7, database | 2026-06-30 |
| 7.2 | Sprint 7.2: OAuthProviderPort (интерфейс) | sprint-7, backend | 2026-06-30 |
| 7.3 | Sprint 7.3: Основные endpoints | sprint-7, backend | 2026-06-30 |
| 7.4 | Sprint 7.4: Синхронизация профиля | sprint-7, backend | 2026-06-29 |

### Sprint 8: Rate Limiting (3 subtasks)

| Issue | Title | Label | Due Date |
|-------|-------|-------|----------|
| 8.1 | Sprint 8.1: Redis rate limiting repository | sprint-8, redis | 2026-07-02 |
| 8.2 | Sprint 8.2: Rate limiting filter | sprint-8, security | 2026-07-02 |
| 8.3 | Sprint 8.3: Audit logging rate limiting | sprint-8, backend | 2026-07-03 |

### Sprint 9: Admin Console (2 subtasks)

| Issue | Title | Label | Due Date |
|-------|-------|-------|----------|
| 9.1 | Sprint 9.1: Admin endpoints | sprint-9, backend | 2026-07-04 |
| 9.2 | Sprint 9.2: Unit тесты Admin endpoints | sprint-9, testing | 2026-07-05 |

### Sprint 10: Аудит, логирование, финальные доработки (5 subtasks)

| Issue | Title | Label | Due Date |
|-------|-------|-------|----------|
| 10.1 | Sprint 10.1: Audit logs | sprint-10, backend | 2026-07-07 |
| 10.2 | Sprint 10.2: Security logging filter | sprint-10, security | 2026-07-07 |
| 10.3 | Sprint 10.3: Kafka producer для всех событий | sprint-10, kafka | 2026-07-06 |
| 10.4 | Sprint 10.4: Health checks и метрики | sprint-10, ops | 2026-07-07 |
| 10.5 | Sprint 10.5: OpenAPI documentation | sprint-10, backend | 2026-07-08 |

---

## API для создания задач (GitLab REST API)

### Create Epic Issue

```bash
curl --header "PRIVATE-TOKEN: YOUR_TOKEN" \
  --header "Content-Type: application/json" \
  --request POST \
  --data '{"title":"Sprint N: [название]","description":"[описание]","labels":"sprint-N,epic","due_date":"2026-07-15"}' \
  https://alxsshv.com/api/v4/projects/2/issues
```

### Create Subtask (with parent)

```bash
curl --header "PRIVATE-TOKEN: YOUR_TOKEN" \
  --header "Content-Type: application/json" \
  --request POST \
  --data '{"title":"Sprint N.M: [название]","description":"[описание]","labels":"sprint-N,backend","due_date":"2026-06-12","parent_issue_iid":N}' \
  https://alxsshv.com/api/v4/projects/2/issues
```

---

## Implementation Notes

1. **Milestone:** Все задачи должны быть привязаны к milestone "MVP" (due: 2026-07-15)
2. **Labels:** Используйте labels sprint-1 through sprint-10 для фильтрации
3. **Dependencies:** Subtasks автоматически связаны с epic через `parent_issue_iid`
4. **Due Dates:** Установите дедлайны по спринтам (каждый спринт ~5 дней)

---

## Завершено

- [x] Созданы документы в `docs/tasks/`
- [x] Созданы GitLab Labels (sprint-1 through sprint-10)
- [x] Создан Milestone "MVP"
- [x] Подготовлены все Epic и Subtasks в формате JSON
- [ ] Созданы задачи в GitLab (требуется API токен)
