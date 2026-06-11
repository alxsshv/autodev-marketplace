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

## GitLab Issues

| Issue | Название | Статус | Приоритет | Оценка | Блокирует |
|-------|----------|--------|-----------|--------|-----------|
| TS-003-1.1 | Создание Gradle проекта | To Do | Normal | 2 часа | Sprint 2 |
| TS-003-1.2 | PostgreSQL миграции (users, roles, indexes) | To Do | Normal | 8 часов | Sprint 2 |
| TS-003-1.3 | Redis конфигурация | To Do | Normal | 4 часа | Sprint 2 |
| TS-003-1.4 | Entity модели | To Do | Normal | 6 часов | Sprint 2 |
| TS-003-1.5 | DTO модели | To Do | Normal | 4 часа | Sprint 2 |
| TS-003-1.6 | Integration тесты | To Do | Normal | 12 часов | Sprint 2 |

---

## Зависимости

**Этот спринт блокирует:**
- Sprint 2 (JWT Token Service)
- Sprint 3 (Spring Security Configuration)
- Sprint 4 (API endpoints)
- Sprint 5 (Keycloak Integration)
- Sprint 6 (Platform Service Integration)
- Sprint 7 (OAuth2 Provider Management)
- Sprint 8 (Rate Limiting)

**Зависит от:**
- PostgreSQL инфраструктура (база данных)
- Redis инфраструктура (кэш)

---

## Критерии готовности

- [ ] Все миграции Liquibase применены успешно
- [ ] Redis подключен и работает (Testcontainers)
- [ ] Покрытие unit тестов >80% для базовых компонентов
- [ ] Интеграционные тесты проходят (Testcontainers PostgreSQL + Redis)
- [ ] Демонстрация: проект компилируется, миграции работают, Redis подключен
