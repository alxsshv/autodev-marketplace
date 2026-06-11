# Sprint 1: Базовая инфраструктура

**Эпик:** TS-003-SPRINT-1  
**Статус:** To Do  
**Приоритет:** Normal  
**Оценка времени:** 5 дней (40 часов)  
**Блокирует:** Sprint 2, Sprint 3, Sprint 4, Sprint 5, Sprint 6, Sprint 7, Sprint 8  
**Зависимости:** PostgreSQL инфраструктура, Redis инфраструктура

---

## Описание

Создание базовой инфраструктуры проекта Auth Service:
- Настройка Gradle проекта с необходимыми зависимостями
- PostgreSQL миграции через Liquibase (13 файлов)
- Redis конфигурация и подключение
- Базовые entity модели и DTO
- Интеграционные тесты через Testcontainers

---

## Задачи

### 1.1. Создание Gradle проекта

**Оценка:** 2 часа  
**Критерии приёмки:**
- AC-1.1: `build.gradle.kts` содержит зависимости: Spring Boot 3.4.5, Spring Security 6.4.5, Spring Data Redis 3.4.5, Liquibase, PostgreSQL, Testcontainers
- AC-1.2: `settings.gradle.kts` содержит модуль `auth-service`
- AC-1.3: `application.yml` с базовой конфигурацией (server.port, logging)
- AC-1.4: `application-test.yml` для тестов (Testcontainers)

**Зависимости:** Нет

---

### 1.2. PostgreSQL миграции (users, roles, indexes)

**Оценка:** 8 часов  
**Критерии приёмки:**
- AC-1.5: Миграция `03-06-2026-create-table-users.sql` создает таблицу `auth.users`
- AC-1.6: Миграция `03-06-2026-create-table-roles.sql` создает таблицу `auth.roles`
- AC-1.7: Миграция `03-06-2026-insert-initial-roles.sql` вставляет 4 роли (BUYER, SELLER, MODERATOR, ADMIN)
- AC-1.8: Миграция `03-06-2026-create-indexes.sql` создает индексы для производительности

**Зависимости:** 1.1 (Gradle проект)

---

### 1.3. Redis конфигурация

**Оценка:** 4 часа  
**Критерии приёмки:**
- AC-1.9: `RedisConfig.java` конфигурирует `RedisTemplate` и `StringRedisTemplate`
- AC-1.10: `RedisProperties.java` через `@ConfigurationProperties` для настроек Redis
- AC-1.11: `RedisConnectionFactory` подключается к Testcontainers Redis в тестах
- AC-1.12: Integration test `RedisConnectionTest` проверяет подключение (200 OK)

**Зависимости:** 1.1 (Gradle проект)

---

### 1.4. Entity модели

**Оценка:** 6 часов  
**Критерии приёмки:**
- AC-1.13: `User.java` — entity с полями: id, keycloak_user_id, email, role, enabled, owner_id
- AC-1.14: `Role.java` — entity с полями: id, name, description
- AC-1.15: `UserRepository.java` — Spring Data JPA репозиторий с методами findByEmail, findByKeycloakUserId
- AC-1.16: `RoleRepository.java` — Spring Data JPA репозиторий с методом findByName

**Зависимости:** 1.2 (PostgreSQL миграции)

---

### 1.5. DTO модели

**Оценка:** 4 часа  
**Критерии приёмки:**
- AC-1.17: `LoginRequest.java` — DTO для входа (username, password)
- AC-1.18: `TokenPair.java` — DTO для ответа с access_token и refresh_token
- AC-1.19: `UserResponse.java` — DTO для ответа с данными пользователя
- AC-1.20: `RoleResponse.java` — DTO для ответа с ролями

**Зависимости:** 1.1 (Gradle проект)

---

### 1.6. Integration тесты

**Оценка:** 12 часов  
**Критерии приёмки:**
- AC-1.21: `UserRepositoryTest` — тесты findByEmail, findByKeycloakUserId (Testcontainers PostgreSQL)
- AC-1.22: `RedisConnectionTest` — тест подключения к Redis (Testcontainers Redis)
- AC-1.23: `LiquibaseTest` — тест применения миграций (Testcontainers PostgreSQL + Liquibase)
- AC-1.24: `ApplicationStartupTest` — тест запуска приложения без ошибок

**Зависимости:** 1.2 (PostgreSQL миграции), 1.3 (Redis конфигурация), 1.4 (Entity модели)

---

## Критерии готовности спринта

- [ ] Все миграции Liquibase применены успешно
- [ ] Redis подключен и работает (Testcontainers)
- [ ] Покрытие unit тестов >80% для базовых компонентов
- [ ] Интеграционные тесты проходят (Testcontainers PostgreSQL + Redis)
- [ ] Демонстрация: проект компилируется, миграции работают, Redis подключен

---

## Зависимости от других спринтов

**Этот спринт блокирует:**
- Sprint 2 (JWT Token Service) — нужен Redis для revoked tokens
- Sprint 3 (Spring Security) — нужен User repository для Authentication
- Sprint 4 (API endpoints) — нужен User entity для входа
- Sprint 5 (Keycloak Integration) — нужен User entity для синхронизации
- Sprint 6 (Platform Service Integration) — нужен User entity для связи с профилем
- Sprint 7 (OAuth2 Provider Management) — нужен User entity для связи с провайдерами
- Sprint 8 (Rate Limiting) — нужен Redis (уже готов)

---

## GitLab issue structure

```
TS-003: Microservice «Auth Service» для AutoDev Marketplace (Epic)
├── TS-003-SPRINT-1: Sprint 1: Базовая инфраструктура (Epic)
│   ├── TS-003-1.1: Создание Gradle проекта (Issue)
│   ├── TS-003-1.2: PostgreSQL миграции (users, roles, indexes) (Issue)
│   ├── TS-003-1.3: Redis конфигурация (Issue)
│   ├── TS-003-1.4: Entity модели (Issue)
│   ├── TS-003-1.5: DTO модели (Issue)
│   └── TS-003-1.6: Integration тесты (Issue)
```

---

## Примечания

- Все миграции должны быть идемпотентными (добавление колонок, индексов)
- Redis key-structure готов к использованию в Sprint 2 (revoked tokens, rate limiting)
- Entity модели должны соответствовать PostgreSQL schema из ТЗ TS-003
