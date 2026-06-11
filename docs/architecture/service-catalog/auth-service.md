# Auth Service

**Версия документа:** 1.0  
**Дата создания:** 2026-06-03  
**Последнее обновление:** 2026-06-03

---

## Обзор

Auth Service — это центральный сервис аутентификации и авторизации для всей системы. Он интегрируется с Keycloak для управления пользователями и генерации JWT токенов, а также хранит дополнительную информацию о пользователях в PostgreSQL.

---

## Бизнес-функция

Auth Service обеспечивает:
- Централизованную аутентификацию через Keycloak
- Генерацию и валидацию JWT токенов
- Управление пользователями и ролями
- Ассоциацию между Keycloak user ID и внутренним пользователем
- Кэширование данных пользователей в Redis
- Асинхронную синхронизацию событий через Kafka

---

## API endpoints

### Authentication & Authorization

| Method | Path | Описание | RBAC |
|--------|------|----------|------|
| `POST` | `/api/v1/auth/login` | Аутентификация пользователя | Публичный |
| `POST` | `/api/v1/auth/refresh` | Обновление токена | Публичный |
| `POST` | `/api/v1/auth/logout` | Выход из системы | BUYER, SELLER, MODERATOR, ADMIN |
| `GET` | `/api/v1/auth/me` | Получение текущего пользователя | BUYER, SELLER, MODERATOR, ADMIN |
| `POST` | `/api/v1/auth/verify` | Проверка валидности токена | Публичный |

### User Management

| Method | Path | Описание | RBAC |
|--------|------|----------|------|
| `GET` | `/api/v1/auth/users/{id}` | Получение пользователя по ID | ADMIN, MODERATOR |
| `PUT` | `/api/v1/auth/users/{id}` | Обновление пользователя | ADMIN, MODERATOR |
| `DELETE` | `/api/v1/auth/users/{id}` | Удаление пользователя | ADMIN |

### Role Management

| Method | Path | Описание | RBAC |
|--------|------|----------|------|
| `GET` | `/api/v1/auth/roles` | Список всех ролей | ADMIN |
| `POST` | `/api/v1/auth/roles` | Создание роли | ADMIN |

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
    first_name          VARCHAR(255)   NULL,
    last_name           VARCHAR(255)   NULL,
    phone               VARCHAR(50)    NULL,
    role                VARCHAR(50)    NOT NULL,
    enabled             BOOLEAN        NOT NULL   DEFAULT TRUE,
    created_at          TIMESTAMP      NOT NULL   DEFAULT CURRENT_TIMESTAMP,
    updated_at        	TIMESTAMP      NOT NULL   DEFAULT CURRENT_TIMESTAMP
);
```

#### Таблица: `roles`
```sql
CREATE TABLE auth.roles (
    id                  BIGSERIAL      PRIMARY KEY,
    name                VARCHAR(50)    NOT NULL   UNIQUE,
    description         VARCHAR(255)   NULL,
    created_at          TIMESTAMP      NOT NULL   DEFAULT CURRENT_TIMESTAMP
);
```

#### Таблица: `permissions`
```sql
CREATE TABLE auth.permissions (
    id                  BIGSERIAL      PRIMARY KEY,
    name                VARCHAR(100)   NOT NULL   UNIQUE,
    description         VARCHAR(255)   NULL,
    created_at          TIMESTAMP      NOT NULL   DEFAULT CURRENT_TIMESTAMP
);
```

---

## Архитектурные решения

### Интеграция с Keycloak
**Преимущества:**
- Уже установлен и настроен
- Поддержка OAuth2/OpenID Connect
- Встроенные механизмы аутентификации (2FA, соцсети)
- Админка для управления пользователями

**Реализация:**
- Keycloak REST API для управления пользователями
- JWT токены для аутентификации
- Webhook для событий (user_created, user_deleted)

### Кэширование в Redis
**Использование:**
- Кэш JWT токенов (12 часов TTL)
- Кэш данных пользователей (1 час TTL)
- Rate limiting counters

**Ключи:**
```
auth:token:{token_hash}
auth:user:{user_id}
auth:rate_limit:{ip_address}
```

### Асинхронная синхронизация (Kafka)
**События:**
- `auth.user_registered` — новый пользователь
- `auth.user_updated` — обновление пользователя
- `auth.user_deleted` — удаление пользователя

**Подписчики:**
- User Service — создаёт профиль пользователя
- Notification Service — отправляет welcome email

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
- Auth Service → Keycloak API (управление пользователями)

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

## Миграции базы данных

### Liquibase changelog
```
src/main/resources/db/changelog/
├── db.changelog-master.yaml
└── v1.0.0/
    ├── 03-06-2026-create-table-users.sql
    └── 03-06-2026-create-table-roles.sql
```

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
- [ ] Собственный интерфейс управления пользователями
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
