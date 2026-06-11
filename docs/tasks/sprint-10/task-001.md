# Sprint 10: Аудит, логирование, финальные доработки

**Эпик:** TS-003-SPRINT-10  
**Статус:** To Do  
**Приоритет:** Normal  
**Оценка времени:** 4 дня (32 часа)  
**Блокирует:** Нет (финальный спринт для MVP)  
**Зависимости:** Все предыдущие спринты

---

## Описание

Финальные доработки для продакшена:
- Аудит логи событий (login, logout, token refresh, invalid credentials)
- Security logging filter (замена Authorization заголовка на ***)
- Kafka producer для всех событий
- Health checks и метрики
- Документация OpenAPI

---

## Задачи

### 10.1. Audit logs

**Оценка:** 8 часов  
**Критерии приёмки:**
- AC-10.1: `AuditLogger.logEvent()` пишет событие в `auth.audit_logs` (event_type, user_id, ip_address, user_agent, timestamp)
- AC-10.2: Миграция `03-06-2026-create-table-audit-logs.sql` создает таблицу с индексами
- AC-10.3: События: LOGIN, LOGOUT, TOKEN_REFRESH, INVALID_CREDENTIALS
- AC-10.4: Unit тест `AuditLoggerTest.logEvent()` проверяет запись в базу

**Зависимости:** Sprint 1 (PostgreSQL, Base models)

---

### 10.2. Security logging filter

**Оценка:** 6 часов  
**Критерии приёмки:**
- AC-10.5: `SecurityLoggingFilter` заменяет `Authorization` заголовок на `***` во всех логах
- AC-10.6: Использование MDC для передачи заголовков в логи
- AC-10.7: Unit тест `SecurityLoggingFilterTest.maskAuthorizationHeader()` проверяет маскировку

**Зависимости:** Sprint 1 (Gradle проект)

---

### 10.3. Kafka producer для всех событий

**Оценка:** 8 часов  
**Критерии приёмки:**
- AC-10.8: `AuthEventProducer` публикует `auth.user_authenticated` при входе
- AC-10.9: `AuthEventProducer` публикует `auth.user_logout` при выходе
- AC-10.10: `PlatformEventProducer` публикует `platform.user_registered` при регистрации
- AC-10.11: `PlatformEventProducer` публикует `platform.user_verified` при верификации
- AC-10.12: Unit тест `AuthEventProducerTest.allEvents()` проверяет публикацию всех событий

**Зависимости:** Sprint 1 (Gradle проект с Kafka dependency), Sprint 6 (Platform Service Integration)

---

### 10.4. Health checks и метрики

**Оценка:** 6 часов  
**Критерии приёмки:**
- AC-10.13: `HealthIndicator` для Redis (проверка подключения)
- AC-10.14: `HealthIndicator` для Keycloak (проверка доступности через OIDC discovery endpoint)
- AC-10.15: `/actuator/health` возвращает статус по всем зависимостям
- AC-10.16: `/actuator/prometheus` возвращает метрики для Prometheus
- AC-10.17: Unit тест `HealthCheckTest` проверяет健康 checks

**Зависимости:** Sprint 1 (Redis, PostgreSQL), Sprint 5 (Keycloak Integration)

---

### 10.5. OpenAPI documentation

**Оценка:** 4 часа  
**Критерии приёмки:**
- AC-10.18: `/api-docs` возвращает OpenAPI 3.0 спецификацию
- AC-10.19: Документация содержит все endpoints: login, refresh, logout, me, validate-token, external/*, admin/*
- AC-10.20: Unit тест `OpenApiSpecTest` проверяет валидность спецификации

**Зависимости:** Все предыдущие спринты (для полной документации)

---

## Критерии готовности спринта

- [ ] Аудит логи пишутся в `auth.audit_logs` для всех событий
- [ ] Security logging filter маскирует `Authorization` заголовок в логах
- [ ] Kafka producer публикует все события (auth.*, platform.*)
- [ ] `/actuator/health` возвращает статус по всем зависимостям
- [ ] `/actuator/prometheus` возвращает метрики
- [ ] OpenAPI 3.0 спецификация доступна и валидна
- [ ] Демонстрация: все endpoints работают, метрики доступны, логи маскируются

---

## Зависимости от других спринтов

**Этот спринт финальный** — требует выполнения всех предыдущих спринтов.

**Зависит от:**
- Sprint 1 (Base models, Redis, PostgreSQL)
- Sprint 2 (JWT Token Service)
- Sprint 3 (Security Configuration)
- Sprint 4 (API endpoints)
- Sprint 5 (Keycloak Integration)
- Sprint 6 (Platform Service Integration)
- Sprint 7 (OAuth2 Provider Management)
- Sprint 8 (Rate Limiting)
- Sprint 9 (Admin Console)

---

## GitLab issue structure

```
TS-003-SPRINT-9: Sprint 9: Admin Console (Epic)
├── TS-003-SPRINT-10: Sprint 10: Аудит, логирование, финальные доработки (Epic)
│   ├── TS-003-10.1: Audit logs (Issue)
│   ├── TS-003-10.2: Security logging filter (Issue)
│   ├── TS-003-10.3: Kafka producer для всех событий (Issue)
│   ├── TS-003-10.4: Health checks и метрики (Issue)
│   └── TS-003-10.5: OpenAPI documentation (Issue)
```

---

## Примечания

- Аудит логи пишутся асинхронно (не блокируют выполнение)
- Security logging filter должен быть первым в цепочке фильтров
- Kafka producer использует fire-and-forget стратегию (не блокирует выполнение при ошибках)
- Health checks позволяют Kubernetes автоматически перезапускать unhealthy инстансы
- OpenAPI спецификация доступна через Springdoc OpenAPI dependency
