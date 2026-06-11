# План реализации ТЗ TS-003: Microservice «Auth Service»

**Версия:** 1.0  
**Дата:** 2026-06-07  
**Автор:** GigaCode (аналитик)  
**Статус:** Готово к реализации

---

## Обзор

План декомпозиции комплексного ТЗ TS-003 на 10 спринтов для реализации микросервиса Auth Service в рамках проекта AutoDev Marketplace.

**Общая оценка:** 43 рабочих дня (344 часа) для одного бэкенд-разработчика.

---

## Спринты

### Sprint 1: Базовая инфраструктура (5 дней)

**Статус:** To Do  
**Блокирует:** Sprint 2-10

**Задачи:**
- Создание Gradle проекта с зависимостями (Spring Boot, Security, Redis, Liquibase, PostgreSQL, Testcontainers)
- PostgreSQL миграции (users, roles, initial roles, indexes)
- Redis конфигурация
- Entity модели (User, Role)
- DTO модели (LoginRequest, TokenPair, UserResponse)
- Integration тесты (Testcontainers PostgreSQL + Redis)

**Критерии готовности:**
- Все миграции Liquibase применены успешно
- Redis подключен и работает
- Покрытие unit тестов >80%
- Интеграционные тесты проходят

---

### Sprint 2: JWT Token Service (6 дней)

**Статус:** To Do  
**Блокирует:** Sprint 3-10

**Задачи:**
- Token Service (генерация RS256 JWT токенов)
- JwtParser (валидация JWT с RS256 подписью)
- RedisTokenRepository (revoked tokens, used refresh tokens)
- Token Pair DTO
- Unit тесты JWT Token Service

**Критерии готовности:**
- Генерация access token (15 мин TTL, RS256)
- Генерация refresh token (7 дней TTL, одноразовый)
- Валидация JWT с RS256 подписью
- Redis хранит revoked tokens
- Покрытие unit тестов >80%

---

### Sprint 3: Spring Security Configuration (4 дня)

**Статус:** To Do  
**Блокирует:** Sprint 4-10

**Задачи:**
- JwtAuthenticationFilter (валидация JWT из заголовка)
- SecurityConfig с @EnableMethodSecurity
- @PreAuthorize для защиты endpoints
- Authentication объект
- Unit тесты Security Configuration

**Критерии готовности:**
- JwtAuthenticationFilter валидирует токены
- SecurityConfig защищает endpoints через @PreAuthorize
- 401 Unauthorized для невалидного токена
- 403 Forbidden для недостатка прав
- Покрытие unit тестов >80%

---

### Sprint 4: Основные API endpoints (5 дней)

**Статус:** To Do  
**Блокирует:** Sprint 5-7

**Задачи:**
- POST /api/v1/auth/login (вход пользователя)
- POST /api/v1/auth/refresh (обновление токенов)
- POST /api/v1/auth/logout (выход пользователя)
- GET /api/v1/auth/me (информация о пользователе)
- GET /api/v1/auth/validate-token (проверка токена для API Gateway)
- Unit тесты API endpoints

**Критерии готовности:**
- All endpoints работают
- Rate limiting (5/5min для login)
- Revoked tokens проверяются через Redis
- Покрытие unit тестов >80%

---

### Sprint 5: Keycloak Integration (5 дней)

**Статус:** To Do  
**Блокирует:** Sprint 6-7

**Задачи:**
- Keycloak Admin Client (адаптер для Admin API)
- OIDC Login через Keycloak (grant_type=password)
- Admin API endpoints (создание/список/обновление ролей)
- Integration тесты (Testcontainers Keycloak)
- Keycloak configuration

**Критерии готовности:**
- Keycloak OIDC login работает
- Keycloak Admin API работает
- @PreAuthorize("hasRole('ADMIN')") защищает /admin/**
- Integration тесты проходят

---

### Sprint 6: Platform Service Integration (4 дня)

**Статус:** To Do  
**Блокирует:** Sprint 7

**Задачи:**
- PlatformServicePort (интерфейс для синхронизации профиля)
- Синхронизация auth.users ↔ platform_service.user_profiles
- GET /api/v1/auth/me с профилем
- Kafka producer событий (auth.*, platform.*)

**Критерии готовности:**
- Синхронизация профиля работает
- GET /api/v1/auth/me возвращает данные из обоих сервисов
- Kafka producer публикует события

---

### Sprint 7: OAuth2 Provider Management (4 дня)

**Статус:** To Do  
**Блокирует:** Нет

**Задачи:**
- Таблица oauth_providers
- OAuthProviderPort (интерфейс)
- Основные endpoints (link, unlink, providers)
- Синхронизация профиля из OAuth2 провайдера

**Критерии готовности:**
- POST /api/v1/auth/external/link работает
- POST /api/v1/auth/external/unlink работает
- GET /api/v1/auth/external/providers работает
- Синхронизация профиля работает

---

### Sprint 8: Rate Limiting (3 дня)

**Статус:** To Do  
**Блокирует:** Нет

**Задачи:**
- Redis rate limiting repository
- Rate limiting filter
- Audit logging rate limiting

**Критерии готовности:**
- Лимит входа: 5 попыток за 5 минут
- Лимит привязки провайдеров: 3 попытки за 10 минут
- Лимит админ-операций: 10 попыток за 1 минуту
- 429 Too Many Requests при превышении

---

### Sprint 9: Admin Console (3 дня)

**Статус:** To Do  
**Блокирует:** Нет

**Задачи:**
- Admin endpoints (GET/POST /admin/users, PATCH /admin/users/{id}/roles)
- Unit тесты Admin endpoints

**Критерии готовности:**
- GET /api/v1/admin/users возвращает список пользователей
- POST /api/v1/admin/users создает пользователя
- PATCH /api/v1/admin/users/{id}/roles обновляет роли
- @PreAuthorize("hasRole('ADMIN')") защищает endpoints

---

### Sprint 10: Аудит, логирование, финальные доработки (4 дня)

**Статус:** To Do  
**Блокирует:** Нет

**Задачи:**
- Audit logs (события login, logout, token refresh, invalid credentials)
- Security logging filter (маскировка Authorization заголовка)
- Kafka producer для всех событий
- Health checks и метрики (/actuator/health, /actuator/prometheus)
- OpenAPI documentation

**Критерии готовности:**
- Аудит логи пишутся в auth.audit_logs
- Security logging filter маскирует Authorization
- Kafka producer публикует все события
- Health checks и метрики работают
- OpenAPI 3.0 спецификация доступна

---

## GitLab Issue Structure

```
TS-003: Microservice «Auth Service» для AutoDev Marketplace (Epic)
├── TS-003-SPRINT-1: Sprint 1: Базовая инфраструктура (Epic)
│   ├── TS-003-1.1: Создание Gradle проекта
│   ├── TS-003-1.2: PostgreSQL миграции
│   ├── TS-003-1.3: Redis конфигурация
│   ├── TS-003-1.4: Entity модели
│   ├── TS-003-1.5: DTO модели
│   └── TS-003-1.6: Integration тесты
├── TS-003-SPRINT-2: Sprint 2: JWT Token Service (Epic)
│   ├── TS-003-2.1: Token Service
│   ├── TS-003-2.2: JwtParser
│   ├── TS-003-2.3: RedisTokenRepository
│   ├── TS-003-2.4: Token Pair DTO
│   └── TS-003-2.5: Unit тесты JWT Token Service
├── TS-003-SPRINT-3: Sprint 3: Spring Security Configuration (Epic)
│   ├── TS-003-3.1: JwtAuthenticationFilter
│   ├── TS-003-3.2: SecurityConfig
│   ├── TS-003-3.3: Authentication объект
│   ├── TS-003-3.4: Security error responses
│   └── TS-003-3.5: Unit тесты Security Configuration
├── TS-003-SPRINT-4: Sprint 4: Основные API endpoints (Epic)
│   ├── TS-003-4.1: POST /api/v1/auth/login
│   ├── TS-003-4.2: POST /api/v1/auth/refresh
│   ├── TS-003-4.3: POST /api/v1/auth/logout
│   ├── TS-003-4.4: GET /api/v1/auth/me
│   ├── TS-003-4.5: GET /api/v1/auth/validate-token
│   └── TS-003-4.6: Unit тесты API endpoints
├── TS-003-SPRINT-5: Sprint 5: Keycloak Integration (Epic)
│   ├── TS-003-5.1: Keycloak Admin Client
│   ├── TS-003-5.2: OIDC Login через Keycloak
│   ├── TS-003-5.3: Admin API endpoints
│   ├── TS-003-5.4: Integration тесты Keycloak
│   └── TS-003-5.5: Keycloak configuration
├── TS-003-SPRINT-6: Sprint 6: Platform Service Integration (Epic)
│   ├── TS-003-6.1: PlatformServicePort
│   ├── TS-003-6.2: Синхронизация профиля пользователя
│   ├── TS-003-6.3: GET /api/v1/auth/me с профилем
│   └── TS-003-6.4: Kafka producer событий
├── TS-003-SPRINT-7: Sprint 7: OAuth2 Provider Management (Epic)
│   ├── TS-003-7.1: oauth_providers таблица
│   ├── TS-003-7.2: OAuthProviderPort
│   ├── TS-003-7.3: Основные endpoints
│   └── TS-003-7.4: Синхронизация профиля
├── TS-003-SPRINT-8: Sprint 8: Rate Limiting (Epic)
│   ├── TS-003-8.1: Redis rate limiting repository
│   ├── TS-003-8.2: Rate limiting filter
│   └── TS-003-8.3: Audit logging rate limiting
├── TS-003-SPRINT-9: Sprint 9: Admin Console (Epic)
│   ├── TS-003-9.1: Admin endpoints
│   └── TS-003-9.2: Unit тесты Admin endpoints
└── TS-003-SPRINT-10: Sprint 10: Аудит, логирование, финальные доработки (Epic)
    ├── TS-003-10.1: Audit logs
    ├── TS-003-10.2: Security logging filter
    ├── TS-003-10.3: Kafka producer для всех событий
    ├── TS-003-10.4: Health checks и метрики
    └── TS-003-10.5: OpenAPI documentation
```

---

## Риски и Mitigation

| Риск | Описание | Mitigation |
|------|----------|------------|
| **Высокий** | Keycloak Admin API нестабилен | Заготовить adapter с mock в тестах (Testcontainers Keycloak) |
| **Средний** | Redis память (revoked tokens) | Настроить EXPIRE (TTL) автоматически — реализовано в миграциях |
| **Средний** | Platform Service API нестабилен | Интерфейс PlatformServicePort — можно подменить mock |
| **Низкий** | Неточность счётчиков Redis | Использовать Redis INCR, EXPIRE и SETNX как в ТЗ |

---

## Заключение

План декомпозиции ТЗ TS-003 на 10 спринтов готов к реализации. Каждый спринт имеет четкие критерии готовности и зависит от предыдущих спринтов в соответствии с архитектурой проекта.

**Рекомендуется начать с Sprint 1 (Базовая инфраструктура).**
