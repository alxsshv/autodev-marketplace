# Auth Service

**Версия документа:** 1.0  
**Дата создания:** 2026-06-03  
**Последнее обновление:** 2026-06-03

---

## Зачем нужен Auth Service в архитектуре с Keycloak?

**Вопрос:** Если Keycloak уже управляет аутентификацией и ролями, зачем нужен auth-service?

**Ответ:** Auth Service служит **оберткой и адаптером** между Keycloak и микросервисной архитектурой:

1. **Синхронизация пользователей**
   - Keycloak хранит только аутентификационные данные (email, enabled)
   - PostgreSQL хранит связи с бизнес-данными (platform_service.users, loyalty_accounts)
   - Auth Service синхронизирует эти данные через Kafka события

2. **Кэширование для производительности**
   - Keycloak может быть узким местом при большом количестве запросов
   - JWT токены кэшируются в Redis (12 часов TTL)
   - Публичные ключи Keycloak кэшируются в Redis для быстрой валидации
   - Данные пользователей кэшируются в Redis (1 час TTL)

3. **Вспомогательные операции**
   - Logout (отзыв токена)
   - Get current user (из кэша Redis)
   - Get user by ID (из PostgreSQL)
   - Получение публичных ключей для валидации JWT

**ВАЖНО:** Auth Service **НЕ управляет** бизнес-данными профиля (first_name, last_name, phone). Обновление этих данных осуществляется через `platform-service`. Auth Service только синхронизирует данные из Keycloak в PostgreSQL и предоставляет:
- GET `/api/v1/auth/me` — получение данных текущего пользователя из кэша Redis
- GET `/api/v1/auth/users/{id}` — получение пользователя по ID для синхронизации с PostgreSQL (только для ADMIN)

4. **Изоляция от Keycloak**
   - Микросервисы не зависят напрямую от Keycloak API
   - Изменения в Keycloak не требуют изменений в сервисах
   - Централизованная точка интеграции

**Ключевое различие:**
- Keycloak: управление ролями, аутентификация, JWT токены
- Auth Service: синхронизация пользователей, кэширование, вспомогательные операции

---

## Обзор

Auth Service — это центральный сервис аутентификации, который служит оберткой над Keycloak для интеграции с микросервисной архитектурой. В новой архитектуре Keycloak является **единственным источником правды для ролей**, а auth-service отвечает за:
- Синхронизацию пользователей между Keycloak и PostgreSQL (для аутентификационных данных)
- Кэширование JWT токенов в Redis для производительности (12 часов TTL)
- Асинхронную обработку событий через Kafka (user_registered, user_updated, user_deleted, user_enabled, user_disabled)
- Предоставление REST API для вспомогательных операций с пользователями:
  - `GET /api/v1/auth/me` — получение текущего пользователя из кэша (BUYER, SELLER, MODERATOR, ADMIN)
  - `GET /api/v1/auth/users/{id}` — получение пользователя по ID для синхронизации (ADMIN only)

---

## Бизнес-функция

Auth Service обеспечивает:
- **Синхронизацию пользователей** между Keycloak и PostgreSQL (для аутентификационных данных)
- **Кэширование JWT токенов** в Redis для производительности (12 часов TTL)
- **Кэширование данных пользователей** в Redis (1 час TTL) для уменьшения нагрузки на БД
- **Асинхронную обработку событий** через Kafka (user_registered, user_updated, user_deleted, user_enabled, user_disabled)
- **Rate limiting** через Redis для защиты от перегрузки
- **REST API** для вспомогательных операций (logout, view profile, get user by ID)

**Важно:** Синхронизация `enabled` статуса происходит через Keycloak Events Provider с настройкой событий `user_enabled` и `user_disabled`. Детали в `security/user-registration-architecture.md`.

**Важно:** Endpoint `GET /api/v1/auth/users/{id}` предоставляется только для ADMIN с целью синхронизации данных между Keycloak и PostgreSQL (для аутентификационных данных). Обновление бизнес-данных профиля (first_name, last_name, phone) осуществляется через `platform-service`.

**Важно:** Endpoint `GET /api/v1/auth/me` предоставляет данные текущего пользователя из кэша Redis (синхронизировано из PostgreSQL).

---

## API endpoints

### Authentication & Authorization

| Method | Path | Описание | RBAC |
|--------|------|----------|------|
| `POST` | `/api/v1/auth/login` | Аутентификация пользователя (выдача токена) | Публичный |
| `POST` | `/api/v1/auth/refresh` | Обновление токена | Публичный |
| `POST` | `/api/v1/auth/logout` | Выход из системы | BUYER, SELLER, MODERATOR, ADMIN |
| `GET` | `/api/v1/auth/me` | Получение текущего пользователя из кэша | BUYER, SELLER, MODERATOR, ADMIN |
| `GET` | `/api/v1/auth/verify` | Проверка валидности токена (через Redis кэш) | Публичный |
| `GET` | `/api/v1/auth/users/{id}` | Получение пользователя по ID (для синхронизации с PostgreSQL) | ADMIN |

**ВАЖНО:** Auth Service **не предоставляет** endpoints для обновления данных профиля (first_name, last_name, phone). Обновление бизнес-данных осуществляется через `platform-service`:
- `GET /api/v1/platform/users/profile` — получение профиля
- `PUT /api/v1/platform/users/profile` — обновление профиля

**ВАЖНО:** Auth Service **не предоставляет** endpoints для создания, обновления и удаления пользователей. Все операции с пользователями осуществляются только через Keycloak Admin Console или API.

**ВАЖНО:** Роли пользователей хранятся только в Keycloak и передаются в JWT токене. Управление ролями осуществляется только через Keycloak Admin Console или API.

**ВАЖНО:** В таблице `platform_service.users` нет поля `role` (удалено в миграции v0.9.0).

---

## Технические детали

### Конфигурация (application.yml)
```yaml
server:
  port: 8082

spring:
  application:
    name: auth-service
  datasource:
    driver-class-name: org.postgresql.Driver
    url: jdbc:postgresql://${AUTH_DB_HOST:localhost}:${AUTH_DB_PORT:5438}/${AUTH_DB_NAME:services}
    username: ${AUTH_DB_USER:postgres}
    password: ${AUTH_DB_PASS:postgres}
  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}
  kafka:
    bootstrap-servers: ${KAFKA_BOOTSTRAP_SERVERS:localhost:9092}
  cloud:
    consul:
      host: ${CONSUL_HOST:localhost}
      port: ${CONSUL_PORT:8500}
      discovery:
        health-check-path: /actuator/health/
        health-check-interval: 15s
        health-check-timeout: 10s
        prefer-ip-address: true
        instance-id: ${spring.application.name}:${random.uuid}

management:
  endpoints:
    web:
      exposure:
        include: health, info, metrics, env, configprops
```

### Зависимости (build.gradle.kts)
```kotlin
dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.cloud:spring-cloud-starter-consul-discovery")
    implementation("org.springframework.cloud:spring-cloud-starter-consul-config")
    implementation("org.springframework.kafka:spring-kafka")
    
    implementation("org.postgresql:postgresql")
    implementation("org.liquibase:liquibase-core")
    
    implementation("io.micrometer:micrometer-registry-prometheus")
}
```

---

## Модель данных

### Схема: `auth`

#### Таблица: `users`
```sql
CREATE TABLE auth.users (
    id                  BIGSERIAL      PRIMARY KEY,
    keycloak_user_id    VARCHAR(255)   NOT NULL   UNIQUE,
    email               VARCHAR(255)   NOT NULL   UNIQUE,
    enabled             BOOLEAN        NOT NULL   DEFAULT TRUE,
    created_at          TIMESTAMP      NOT NULL   DEFAULT CURRENT_TIMESTAMP
);
```

**Индексы:**
- `idx_auth_users_keycloak` ON (keycloak_user_id)

**Примечание:** Эта таблица содержит только аутентификационные данные. Все бизнес-данные пользователя хранятся в `platform_service.users`.

**ВАЖНО:** Роли пользователей хранятся исключительно в Keycloak. В PostgreSQL нет таблиц для хранения ролей (`auth.roles`, `auth.permissions`, `auth.role_permissions` удалены).

---

## Архитектурные решения

### Интеграция с Keycloak
**Преимущества:**
- Уже установлен и настроен
- Поддержка OAuth2/OpenID Connect
- Встроенные механизмы аутентификации (2FA, соцсети)
- Админка для управления пользователями

**Реализация:**
- Keycloak REST API для синхронизации пользователей
- JWT токены для аутентификации (кэшируются в Redis)
- Webhook для событий (user_created, user_deleted)

**Роли пользователей:**
- Хранятся только в Keycloak
- Выдаются в JWT токене при аутентификации
- Проверяются всеми сервисами через валидацию токена

**Кэширование:**
- JWT токены кэшируются в Redis (12 часов TTL)
- Данные пользователей кэшируются в Redis (1 час TTL)
- Публичные ключи Keycloak кэшируются в Redis для валидации токенов

**TTL:**
- Access token: 12 часов
- Refresh token: 7 дней
- JWT cache: 12 часов TTL
- User data: 1 час TTL
- Revoked tokens: до истечения TTL

### Кэширование в Redis
**Использование:**
- Кэш JWT токенов (12 часов TTL) — уменьшение количества запросов к Keycloak
- Кэш данных пользователей (1 час TTL) — уменьшение нагрузки на БД
- Кэш публичных ключей Keycloak — быстрая валидация токенов
- **Кэш revoked токенов (до истечения TTL) — защита от использования отзыванных токенов**
  - Подробнее: [security/revoked-tokens.md](../security/revoked-tokens.md)
- Rate limiting counters (в Redis) — защита от перегрузки

**Ключи:**
```
auth:token:{token_hash}       — JWT токен (валидный или revoked)
auth:user:{keycloak_id}       — данные пользователя
auth:keys                     — публичные ключи Keycloak
auth:rate_limit:{ip_address}  — счетчик для rate limiting
auth:blacklist:{token_hash}   — revoked токен (до истечения TTL)
```

**TTL:**
- Access token: 12 часов
- Refresh token: 7 дней
- JWT cache: 12 часов TTL
- User data: 1 час TTL
- Revoked tokens: до истечения TTL

### Асинхронная синхронизация (Kafka)
**События:**
- `auth.user_registered` — новый пользователь (создание в Keycloak)
- `auth.user_updated` — обновление аутентификационных данных
- `auth.user_deleted` — удаление пользователя (из Keycloak)

**Подписчики:**
- Platform Service — создаёт/обновляет профиль пользователя
- Communication Service — отправляет welcome email

**ВАЖНО:** События содержат только аутентификационные данные (email, keycloak_user_id). Роли не синхронизируются — они хранятся только в Keycloak.

### Синхронизация enabled статуса через Keycloak Webhook

**Когда:** При изменении `enabled` статуса пользователя в Keycloak

**Цепочка операций:**
1. Admin меняет статус в Keycloak Admin Console или через API
2. Keycloak отправляет webhook в Auth Service (event: `user_enabled` или `user_disabled`)
3. Auth Service обновляет PostgreSQL (UPDATE auth.users SET enabled = true/false)
4. Auth Service публикует событие в Kafka (auth.user.enabled или auth.user.disabled)
5. Platform Service получает событие и обновляет профиль пользователя

**События Kafka:**
- `auth.user_enabled` — пользователь активирован
- `auth.user_disabled` — пользователь деактивирован

**Ключ Kafka topic:** `auth.user.enabled` / `auth.user.disabled`

---

## Паттерны проектирования

### Adapter Pattern
KeycloakIntegrationService адаптирует Keycloak REST API к внутреннему интерфейсу.

### Caching Pattern
Redis используется для кэширования:
- Токенов (уменьшение количества запросов к Keycloak)
- Данных пользователей (уменьшение нагрузки на БД)

### Event Sourcing
События Kafka используются для асинхронной синхронизации с другими сервисами.

---

## Межсервисное взаимодействие

### Синхронное (REST API)
- Auth Service → Keycloak API (синхронизация пользователей)
- Auth Service → Redis (кэширование токенов, ключей)
- Другие сервисы → Auth Service (вспомогательные операции)

**ВАЖНО:** Проверка JWT токенов происходит в каждом сервисе напрямую через Redis кэш (без вызова auth-service).

### Асинхронное (Kafka)
- Auth Service → Kafka (события: user_registered, user_updated, user_deleted)
- Auth Service ← Kafka (события: user.profile_created - от User Service)

---

## Безопасность

### Аутентификация
- JWT токены в заголовке `Authorization: Bearer {token}`
- Валидация токена через Keycloak
- Кэширование валидных токенов в Redis

### Авторизация
- Роли: BUYER, SELLER, MODERATOR, ADMIN
- Проверка ролей в аннотации `@PreAuthorize("hasRole('BUYER')")`
- Permisсии для детального контроля

### Шифрование
- Пароли: BCrypt (Spring Security)
- Токены: RS256 (Keycloak)
- БД: TLS (PostgreSQL SSL)

---

## Метрики

| Метрика | Описание | Тип |
|---------|----------|-----|
| `auth_login_attempts_total` | Количество попыток входа | Counter |
| `auth_login_failures_total` | Количество неудачных попыток | Counter |
| `auth_token_validations_total` | Количество валидаций токенов | Counter |
| `auth_token_cache_hits_total` | Количество попаданий в кэш токенов | Counter |
| `auth_user_operations_total` | Количество операций с пользователями | Counter |

---

## SLA/SLO

| Метрика | Целевое значение | Измерение |
|---------|-----------------|-----------|
| Доступность | 99.9% | Uptime (Prometheus) |
| Latency (p95) | <50 мс | Tempo traces |
| Latency (p99) | <200 мс | Tempo traces |
| Ошибки (p99) | <0.5% | Prometheus errors |

---

## Deployment

### Kubernetes
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: auth-service
spec:
  replicas: 2
  template:
    spec:
      containers:
      - name: auth-service
        image: autodev/auth-service:latest
        ports:
        - containerPort: 8082
        env:
        - name: AUTH_DB_HOST
          value: "services-database"
        - name: REDIS_HOST
          value: "redis"
        - name: CONSUL_HOST
          value: "consul"
        resources:
          requests:
            memory: "256Mi"
            cpu: "250m"
          limits:
            memory: "512Mi"
            cpu: "500m"
```

### Docker Compose
```yaml
auth-service:
  build: ./services/auth-service
  container_name: auth-service
  ports:
    - "8083:8082"
  environment:
    - SPRING_PROFILES_ACTIVE=docker
    - AUTH_DB_HOST=services-database
    - REDIS_HOST=redis
    - CONSUL_HOST=consul
  depends_on:
    keycloak:
      condition: service_healthy
    services-database:
      condition: service_healthy
    redis:
      condition: service_healthy
    consul:
      condition: service_healthy
  healthcheck:
    test: ["CMD", "wget", "--spider", "http://localhost:8082/actuator/health"]
    interval: 10s
    timeout: 5s
    retries: 20
    start_period: 40s
```

---

## Важные изменения для Keycloak-as-single-source-of-truth

### Роли пользователей
- **Хранятся только в Keycloak**
- **Выдаются в JWT токене** при аутентификации
- **Проверяются каждым сервисом** через валидацию JWT токена
- **Не синхронизируются** в PostgreSQL (таблицы `auth.roles`, `auth.permissions`, `auth.role_permissions` удалены)
- **Не хранятся** в поле `role` таблицы `platform_service.users` (удалено)

### Управление ролями
- **Только через Keycloak Admin Console**: http://localhost:8090/admin
- **Или через Keycloak Admin API**: http://localhost:8090/admin/realms/{realm}/roles
- **Auth Service НЕ предоставляет endpoints** для управления ролями

### Кэширование
- JWT токены кэшируются в Redis (12 часов TTL) для производительности
- Публичные ключи Keycloak кэшируются в Redis для быстрой валидации
- **Revoked токены кэшируются в Redis до истечения TTL** для защиты от использования
- Роли пользователя кэшируются внутри JWT токена (не требуется отдельный кэш ролей)

### Миграции базы данных

#### Структура миграций

```
src/main/resources/db/changelog/
├── db.changelog-master.yaml
├── v0.9.0/
│   └── 14-06-2026-remove-legacy-roles-tables.sql
├── v1.0.0/
│   └── 03-06-2026-create-table-users.sql
└── v1.1.0/
    └── 14-06-2026-add-revoked-tokens-table.sql
```

#### План миграций

| Версия | Описание | Скрипт | Дата |
|--------|----------|--------|------|
| `v0.9.0` | Удаление legacy таблиц ролей и поля `role` | `v0.9.0/14-06-2026-remove-legacy-roles-tables.sql` | 2026-06-14 |
| `v1.0.0` | Создание таблицы `auth.users` | `v1.0.0/03-06-2026-create-table-users.sql` | 2026-06-03 |
| `v1.1.0` | **УДАЛЕНА** - таблица `revoked_tokens` больше не используется (Redis-only подход для MVP) | - | - |

**ВАЖНО:** Для MVP используется Redis-only подход. Таблица `auth.revoked_tokens` удалена из миграций. Все revoked tokens хранятся только в Redis с TTL = expires_at - current_time.

#### Миграция v0.9.0 — Удаление legacy таблиц (для существующих установок)

**Когда применять:** Только для проектов, которые уже имеют legacy таблицы `auth.roles`, `auth.permissions`, `auth.role_permissions` и поле `role` в `platform_service.users`.

**Шаг 1:** Создайте резервную копию базы данных
```bash
pg_dump -U postgres -d services > backup-before-migration.sql
```

**Шаг 2:** Примените миграцию для удаления legacy таблиц
```sql
--liquibase formatted sql
--changeset Aleksey Shvariov:14-06-2026-remove-legacy-roles-tables

-- Удаление legacy таблиц
DROP TABLE IF EXISTS auth.role_permissions;
DROP TABLE IF EXISTS auth.permissions;
DROP TABLE IF EXISTS auth.roles;

-- Удаление поля role из platform_service.users
ALTER TABLE platform_service.users DROP COLUMN IF EXISTS role;

--rollback ALTER TABLE platform_service.users ADD COLUMN role VARCHAR(50) NOT NULL;
--rollback CREATE TABLE auth.roles (...);
--rollback CREATE TABLE auth.permissions (...);
--rollback CREATE TABLE auth.role_permissions (...);
```

**Шаг 3:** Проверьте целостность данных
```sql
-- Проверка отсутствия legacy таблиц
SELECT table_name 
FROM information_schema.tables 
WHERE table_schema = 'auth' 
AND table_name IN ('roles', 'permissions', 'role_permissions');

-- Проверка отсутствия поля role
SELECT column_name 
FROM information_schema.columns 
WHERE table_schema = 'platform_service' 
AND table_name = 'users' 
AND column_name = 'role';
```

**Примечание:** Все операции выполняются во время простоя системы. Рекомендуется провести тестирование на staging окружении перед production.

#### Миграция v1.0.0 — Создание таблицы users

**Когда применять:** Для новых установок или после применения v0.9.0 (если были legacy таблицы).

```sql
--liquibase formatted sql
--changeset Aleksey Shvariov:03-06-2026-create-table-users

-- Схема auth: только аутентификационные данные
CREATE TABLE auth.users (
    id                  BIGSERIAL      PRIMARY KEY,
    keycloak_user_id    VARCHAR(255)   NOT NULL   UNIQUE,
    email               VARCHAR(255)   NOT NULL   UNIQUE,
    enabled             BOOLEAN        NOT NULL   DEFAULT TRUE,
    created_at          TIMESTAMP      NOT NULL   DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_auth_users_keycloak ON auth.users(keycloak_user_id);
CREATE INDEX idx_auth_users_email ON auth.users(email);

--rollback DROP TABLE auth.users;
```

**Примечание:** Эта таблица содержит только аутентификационные данные. Все бизнес-данные пользователя хранятся в `platform_service.users`. Связь между таблицами осуществляется по `auth.users.id` (FK `platform_service.users.user_id`).

---

## Тестирование

### Unit Tests
- Валидация токенов
- Генерация токенов
- Управление пользователями

### Integration Tests
- WireMock для мокирования Keycloak
- Testcontainers для PostgreSQL, Redis, Kafka

### Load Tests
- JMeter: 500 RPS
- Проверка кэширования

---

## Мониторинг

### Grafana Dashboard
- Auth Service Overview
- Login Attempts
- Token Validations
- Cache Hit Ratio

### Алерты
- `HighLoginFailureRate`: >10% неудачных входов за 5 минут
- `TokenValidationErrors`: >1% ошибок валидации за 5 минут
- `ServiceDown`: Сервис недоступен

---

## Риски и ограничения

### Текущие риски
1. **Single point of failure** — если Auth Service упадёт, все пользователи не смогут войти
   - **Mitigation:** 2 реплики с Kubernetes HPA

2. **Keycloak dependency** — все операции зависят от Keycloak
   - **Mitigation:** Кэширование в Redis, fallback на локальные данные

3. **Rate limiting** — без Redis rate limiting не будет работать
   - **Mitigation:** Local rate limiting как fallback

---

## План улучшений

### Short-term (1-2 недели)
- [ ] Реализовать rate limiting в Redis
- [ ] Настроить кэширование профилей пользователей
- [ ] Добавить алерты на аномалии входа

### Medium-term (1-2 месяца)
- [ ] Интеграция с социальными сетями (Google, VK)
- [ ] Поддержка 2FA через TOTP

### Long-term (3-6 месяцев)
- [ ] Decoupled from Keycloak (использовать только JWT)
- [ ] Self-service password reset
- [ ] Audit logging для всех операций

---

## Контакты

- **Owner:** Backend Team
- **Slack:** #auth-service
- **Emergency:** #incident

---

## См. также

- [Консолидация сервисов](../service-consolidation.md) — детали консолидации
- [Системный обзор](../system-overview.md) — обзор архитектуры
- [OpenAPI спецификация](../api-specification/README.md) — API документация
- [ADR-0001](../architecture-decision-records/adr-0001-consolidation.md) — консолидация сервисов
