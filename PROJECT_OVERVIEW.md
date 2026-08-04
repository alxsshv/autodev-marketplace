# Project Overview: autodev-marketplace

# AutoDev Marketplace — Обзор проекта

**AutoDev Marketplace** — это учебная платформа для продажи автозапчастей, построенная по принципам современных распределённых систем. Проект предназначен для обучения Java backend-разработчиков best practices разработки микросервисных приложений на Spring Boot 3.x и Java 17+.

---

## Назначение

**Целевая аудитория:**
- Покупатели автозапчастей
- Продавцы (магазины, разборки)
- Модераторы платформы
- Администраторы системы

**Основные бизнес-ценности:**
- Удобный поиск запчастей по VIN и артикулам
- Безопасная сделка через эскроу-счёт
- Автоматизированная загрузка прайс-листов от продавцов
- Надёжная интеграция с внешними сервисами доставки и оплаты

---

## Бизнес-функциональность (MVP)

### 8 категорий функций (32 функции для MVP)

| Категория | Функции | Сервис |
|-----------|---------|--------|
| **Поиск и каталог** | 7 функций | catalog-service |
| | Поиск по VIN-коду, артикулу, названию | |
| | Поиск по марке/модели, категории | |
| | Фильтрация, сортировка | |
| **Профили** | 3 функции | platform-service |
| | Регистрация, аутентификация, управление профилем | |
| **Управление объявлениями** | 4 функции | catalog-service |
| | Создание, редактирование, архивация, продление | |
| **Взаимодействие** | 1 функция | communication-service |
| | Внутренний чат между покупателями и продавцами | |
| **Оформление заказа** | 7 функций | order-service + payment-service |
| | Корзина, оформление, оплата, выбор доставки | |
| **Управление заказами** | 4 функции | order-service |
| | Просмотр, отслеживание, отмена, возврат | |
| **Рейтинги и отзывы** | 4 функции | platform-service |
| | Оценка, текстовый отзыв, ответ продавца, рейтинг | |
| **Уведомления** | 2 функции | notification-service |
| | Email и SMS уведомления | |

### Что включено в MVP:
- 7 микросервисов с базовой функциональностью
- Direct Keycloak Integration для аутентификации (без auth-service)
- PostgreSQL FTS для поиска (без search-service)
- Упрощённая схема оплаты без эскроу и интеграций с банками
- Базовый чат без шаблонов и истории переписки

### Что исключено из MVP (Post-MVP):
- Auth-service (используется Keycloak напрямую)
- Search-service (PostgreSQL FTS вместо Elasticsearch)
- Модерация (ручная через админку)
- Автодополнение, история поиска, избранное
- Шаблоны ответов, видеоозвонки, история чата
- Калькулятор подбора, каталог аналогов, база знаний
- Аналитика, маркетинг, лояльность

---

## Архитектура микросервисов

### MVP-состав (7 сервисов)

| Сервис | Обязательный | Описание |
|--------|-------------|----------|
| **api-gateway** | ✅ | Единая точка входа, маршрутизация, аутентификация (60-70% реализовано) |
| **catalog-service** | ✅ | Каталог товаров, поиск по VIN/артикулу, управление объявлениями |
| **order-service** | ✅ | Корзина, оформление заказов, управление заказами |
| **payment-service** | ✅ | Обработка платежей (базовая, без эскроу и интеграций) |
| **communication-service** | ✅ | Чат в реальном времени (базовый функционал) |
| **notification-service** | ✅ | Email, SMS уведомления |
| **platform-service** | ✅ | Профили, отзывы, рейтинг (только UserProfileService и ReviewService) |

## Project Metadata

- **Name**: autodev-marketplace
- **Type**: Multi-module Gradle project (Kotlin DSL)
- **Language**: Java 17
- **Framework**: Spring Boot 3.4.5 + Spring Cloud 2024.0.1
- **Build System**: Gradle 8.14.5 with Kotlin DSL
- **Version**: 1.0.0
- **Group**: com.autodev

---

## Module / Submodule Inventory

The project consists of 7 microservices:

| Module                           | Status                   | Description                                                             | Port |
|----------------------------------|--------------------------|-------------------------------------------------------------------------|------|
| `services:api-gateway`           | ✅ Implemented            | Spring Cloud Gateway with reactive stack, rate limiting, JWT validation | 8080 |
| `services:platform-service`      | ⚠️ Partially Implemented | User profiles, reviews, outbox pattern (Spring Data JPA, Kafka)         | 8081 |
| `services:catalog-service`       | 📋 Empty                 | Placeholder (no implementation yet)                                     | -    |
| `services:order-service`         | 📋 Empty                 | Placeholder (no implementation yet)                                     | -    |
| `services:payment-service`       | 📋 Empty                 | Placeholder (no implementation yet)                                     | -    |
| `services:communication-service` | 📋 Empty                 | Placeholder (no implementation yet)                                     | -    |
| `services:notification-service`  | 📋 Empty                 | Placeholder (no implementation yet)                                     | -    |

---

## Dependency Manifest

### Version Catalog (`gradle/libs.versions.toml`)

**Versions:**
| Alias | Version |
|-------|---------|
| `spring-boot` | 3.4.5 |
| `spring-cloud` | 2024.0.1 |
| `apacheCommon` | 2.13.0 (Apache Commons Pool2) |
| `liquibase` | 4.27.0 |
| `lombok` | 1.18.46 |
| `lombokMupstruct` | 0.2.0 (lombok-mapstruct-binding) |
| `mapstruct` | 1.6.3 |
| `mockito` | 5.11.0 |
| `nimbus` | 9.31 (Nimbus JOSE + JWT) |
| `postgres` | 42.7.7 (PostgreSQL JDBC Driver) |
| `reactorTest` | 3.8.0 (Reactor Test) |
| `resilence4j` | 2.2.0 |
| `testcontainers` | 2.0.5 (Testcontainers for Spring Boot) |
| `wiremock` | 3.9.2 |

**Production Libraries:**

| Alias                      | Maven Coordinates                                                     |
|----------------------------|-----------------------------------------------------------------------|
| spring-boot-actuator       | `org.springframework.boot:spring-boot-starter-actuator`               |
| spring-boot-data-jpa       | `org.springframework.boot:spring-boot-starter-data-jpa`               |
| spring-boot-data-redis     | `org.springframework.boot:spring-boot-starter-data-redis`             |
| spring-boot-security       | `org.springframework.boot:spring-boot-starter-security`               |
| spring-boot-resourceServer | `org.springframework.boot:spring-boot-starter-oauth2-resource-server` |
| spring-boot-validation     | `org.springframework.boot:spring-boot-starter-validation`             |
| spring-boot-web            | `org.springframework.boot:spring-boot-starter-web`                    |
| spring-kafka               | `org.springframework.kafka:spring-kafka`                              |
| spring-cloud-consul        | `org.springframework.cloud:spring-cloud-starter-consul-discovery`     |
| spring-cloud-gateway       | `org.springframework.cloud:spring-cloud-starter-gateway`              |
| spring-cloud-loadbalancer  | `org.springframework.cloud:spring-cloud-loadbalancer`                 |
| spring-cloud-stubrunner    | `org.springframework.cloud:spring-cloud-contract-stub-runner`         |
| postgres                   | `org.postgresql:postgresql:42.7.7`                                    |
| liquibase                  | `org.liquibase:liquibase-core:4.27.0`                                 |
| resilence4j-ratelimiter    | `io.github.resilience4j:resilience4j-ratelimiter:2.2.0`               |
| resilence4j-core           | `io.github.resilience4j:resilience4j-core:2.2.0`                      |
| resilence4j-micrometer     | `io.github.resilience4j:resilience4j-micrometer:2.2.0`                |
| resilence4j-spring-boot3   | `io.github.resilience4j:resilience4j-spring-boot3:2.2.0`              |
| micrometer-prometheus      | `io.micrometer:micrometer-registry-prometheus`                        |
| micrometer-tracing-bridge  | `io.micrometer:micrometer-tracing-bridge-otel`                        |
| opentelemetry-exporter     | `io.opentelemetry:opentelemetry-exporter-otlp`                        |
| lombok                     | `org.projectlombok:lombok:1.18.46`                                    |
| lombok-mapstruct-binding   | `org.projectlombok:lombok-mapstruct-binding:0.2.0`                    |
| mapstruct                  | `org.mapstruct:mapstruct:1.6.3`                                       |
| mapstruct-processor        | `org.mapstruct:mapstruct-processor:1.6.3`                             |
| apache-commons-pool        | `org.apache.commons:commons-pool2:2.13.0`                             |

**Test Libraries:**

| Alias                     | Maven Coordinates                                                            |
|---------------------------|------------------------------------------------------------------------------|
| junit-jupiter             | `org.junit.jupiter:junit-jupiter`                                            |
| nimbus-jwt                | `com.nimbusds:nimbus-jose-jwt:9.31`                                          |
| mockito-junit             | `org.mockito:mockito-junit-jupiter:5.11.0`                                   |
| reactor-test              | `io.projectreactor:reactor-test:3.8.0`                                       |
| redis-testcontainers      | `org.testcontainers:testcontainers:2.0.5` + `com.redis:testcontainers-redis` |
| testcontainers            | `org.testcontainers:testcontainers:2.0.5`                                    |
| testcontainers-junit      | `org.testcontainers:junit-jupiter:2.0.5`                                     |
| testcontainers-kafka      | `org.testcontainers:kafka:2.0.5`                                             |
| testcontainers-postgresql | `org.testcontainers:postgresql:2.0.5`                                        |
| wiremock                  | `org.wiremock:wiremock:3.9.2`                                                |
| wiremock-spring           | `org.wiremock.integrations:wiremock-spring-boot:3.9.2`                       |
| spring-boot-test          | `org.springframework.boot:spring-boot-starter-test`                          |

---

## Implemented Classes (Full Inventory)

### api-gateway (`com.autodev.gateway`)

**Source Files:**

| File                                              | Package               | Description                                                                                           |
|---------------------------------------------------|-----------------------|-------------------------------------------------------------------------------------------------------|
| `GatewayApplication.java`                         | `com.autodev.gateway` | Main Spring Boot application class                                                                    |
| `SecurityConfig.java`                             | `com.autodev.gateway` | Security configuration: CORS whitelist, OAuth2 resource server with JWT, disabled CSRF                |
| `RateLimitingFilter.java`                         | `com.autodev.gateway` | Reactive rate-limiting filter per IP using Redis + Resilience4j                                       |
| `RateLimitingProperties.java`                     | `com.autodev.gateway` | Configuration properties for rate limiting (limit-for-period, limit-refresh-period, timeout-duration) |
| `KeycloakReactiveJwtAuthenticationConverter.java` | `com.autodev.gateway` | Custom JWT authentication converter extracting roles from Keycloak tokens                             |

**Integration Test Files (7 total):**

| File                                       | Description                                                                                      |
|--------------------------------------------|--------------------------------------------------------------------------------------------------|
| `AbstractIntegrationTest.java`             | Base class: starts Testcontainers (PostgreSQL, Kafka, Redis), configures WireMock, sets env vars |
| `ApiGatewayRoutingTest.java`               | Tests routing to service stubs via WireMock                                                      |
| `AuthenticationIntegrationTest.java`       | Tests JWT-based authentication on protected routes                                               |
| `CorsIntegrationTest.java`                 | Tests CORS headers and preflight requests                                                        |
| `RateLimitingIntegrationTest.java`         | Tests rate limiting is enforced above threshold                                                  |
| `RateLimitingFailOpenIntegrationTest.java` | Tests fail-open behaviour when Redis is down                                                     |
| `TestRedisContainer.java`                  | Redis container lifecycle manager using `com.redis:testcontainers-redis`                         |

**Endpoints (api-gateway):**

| Path Pattern               | Target Service        | HTTP Methods | Auth Required |
|----------------------------|-----------------------|--------------|---------------|
| `/api/v1/platform/**`      | platform-service      | ALL          | Yes           |
| `/api/v1/catalog/**`       | catalog-service       | ALL          | Yes           |
| `/api/v1/orders/**`        | order-service         | ALL          | Yes           |
| `/api/v1/payments/**`      | payment-service       | ALL          | Yes           |
| `/api/v1/communication/**` | communication-service | ALL          | Yes           |
| `/api/v1/notifications/**` | notification-service  | ALL          | Yes           |
| `/actuator/**`             | api-gateway (local)   | ALL          | No            |

Note: No controllers exist yet in any backend service. All routes return 503 (WireMock stubs in tests) or will forward
to Consul-discovered services.

### platform-service (`com.autodev.platformservice`)

**Entities:**

| File                      | Package                              | Description                                                                                                                                                                                                |
|---------------------------|--------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `AbstractBaseEntity.java` | `com.autodev.platformservice.entity` | Abstract base: `@MappedSuperclass` with `@Id`, `@CreatedDate`, `@LastModifiedDate`, `@Version`                                                                                                             |
| `UserProfileEntity.java`  | `com.autodev.platformservice.entity` | JPA entity for `platform.user_profiles`. Fields: id, keycloakUserId (unique), storeName, storeDescription, storeLogoUrl, avatarUrl, verificationStatus (enum), loyaltyBalance, email, createdAt, updatedAt |
| `ReviewEntity.java`       | `com.autodev.platformservice.entity` | JPA entity for `platform.reviews`. Fields: id, productId, userId, rating, reviewText, sellerReply, createdAt, updatedAt                                                                                    |
| `OutboxEntity.java`       | `com.autodev.platformservice.entity` | JPA entity for `platform.outbox_events`. Fields: id, aggregateType, aggregateId, eventType, topic, payload (JSONB), createdAt, status                                                                      |
| `OutboxStatus.java`       | `com.autodev.platformservice.entity` | Enum: PENDING, SENT, FAILED                                                                                                                                                                                |
| `UserEvents.java`         | `com.autodev.platformservice.entity` | Enum: USER_REGISTERED, USER_PROFILE_UPDATED, USER_VERIFIED                                                                                                                                                 |
| `VerificationStatus.java` | `com.autodev.platformservice.entity` | Enum: UNVERIFIED, VERIFIED, BANNED                                                                                                                                                                         |

**Repositories:**

| File                         | Package                                  | Description                                                                                                                                                                                                                      |
|------------------------------|------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `UserProfileRepository.java` | `com.autodev.platformservice.repository` | `JpaRepository<UserProfileEntity, UUID>`. Custom: `findByKeycloakUserId(String)`, `existsByKeycloakUserId(String)`, `existsByStoreName(String)`                                                                                  |
| `OutboxRepository.java`      | `com.autodev.platformservice.repository` | `JpaRepository<OutboxEntity, UUID>`. Custom: `findByStatusOrderByCreatedAtAsc(OutboxStatus)`                                                                                                                                     |
| `ReviewRepository.java`      | `com.autodev.platformservice.repository` | ⚠️ **Does NOT extend any Spring interface** — custom repository with `findByProductId(String)`, `findByUserId(String)`, `findBySellerReplyIsNull()`. Methods defined but unusable without extending `Repository`/`JpaRepository` |

**DTOs:**

| File                           | Package                           | Description                                                                                                                               |
|--------------------------------|-----------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------|
| `RegisterRequestDto.java`      | `com.autodev.platformservice.dto` | Registration input: username, email, password, storeName, storeDescription                                                                |
| `UpdateProfileRequestDto.java` | `com.autodev.platformservice.dto` | Profile update input: storeName, storeDescription, storeLogoUrl, avatarUrl                                                                |
| `UserProfileResponseDto.java`  | `com.autodev.platformservice.dto` | Profile output: id, keycloakUserId, storeName, storeDescription, storeLogoUrl, avatarUrl, verificationStatus, email, createdAt, updatedAt |

**Mappers:**

| File                     | Package                              | Description                                                                                       |
|--------------------------|--------------------------------------|---------------------------------------------------------------------------------------------------|
| `UserProfileMapper.java` | `com.autodev.platformservice.mapper` | MapStruct: `RegisterRequestDto → UserProfileEntity`, `UserProfileEntity → UserProfileResponseDto` |

**Services:**

| File                       | Package                               | Description                                                                                                                                                                                                                                            |
|----------------------------|---------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `RegistrationService.java` | `com.autodev.platformservice.service` | ⚠️ **Incomplete** — `registerUser(RegisterRequestDto)` has method signature and Keycloak admin client flow stubbed out, but the transactional body and outbox event creation logic is not implemented (method is mostly a skeleton with TODO comments) |
| `UserProfileService.java`  | `com.autodev.platformservice.service` | ⚠️ **Incomplete** — `getCurrentUserProfile()` returns `null` (not implemented). `updateUserProfile(UpdateProfileRequestDto)` is a skeleton. `getUserProfileById(UUID)` is partially implemented                                                        |

**Clients:**

| File                           | Package                                       | Description                                                                                                                                           |
|--------------------------------|-----------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------|
| `KeycloakAdminClient.java`     | `com.autodev.platformservice.client.keycloak` | Feign-like REST client (manual `RestTemplate`-based). Methods: `createUser(UserRepresentation)`, `getUsersByEmail(String)`. ⚠️ May not be fully wired |
| `KeycloakClientConfig.java`    | `com.autodev.platformservice.client.keycloak` | Configuration for Keycloak client: base URL, realm, admin credentials                                                                                 |
| `KeycloakAuthInterceptor.java` | `com.autodev.platformservice.client.keycloak` | `ClientHttpRequestInterceptor` that obtains admin token via `KeycloakTokenProvider` and adds `Authorization: Bearer` header                           |
| `KeycloakTokenProvider.java`   | `com.autodev.platformservice.client.keycloak` | Manages Keycloak admin client credentials grant token, with caching                                                                                   |
| `KeycloakTokenResponse.java`   | `com.autodev.platformservice.client.keycloak` | Record: access_token, expires_in, refresh_expires_in, token_type, not-before-policy, scope                                                            |

**Configuration:**

| File                      | Package                              | Description                                                                                                       |
|---------------------------|--------------------------------------|-------------------------------------------------------------------------------------------------------------------|
| `SecurityConfig.java`     | `com.autodev.platformservice.config` | Security config: OAuth2 resource server with JWT, role-based access (ADMIN, SELLER, BUYER), CORS, disabled CSRF   |
| `KeycloakProperties.java` | `com.autodev.platformservice.config` | `@ConfigurationProperties(prefix = "keycloak.admin")`: baseUrl, realm, username, password, clientId, clientSecret |

**Security Utilities:**

| File                                      | Package                                | Description                                                                              |
|-------------------------------------------|----------------------------------------|------------------------------------------------------------------------------------------|
| `SecurityUtils.java`                      | `com.autodev.platformservice.security` | Static helper: extract current user's Keycloak ID and roles from `SecurityContextHolder` |
| `KeycloakJwtAuthenticationConverter.java` | `com.autodev.platformservice.security` | Extracts roles and Keycloak user ID from JWT claims into `GrantedAuthority` list         |

**Exception Handling:**

| File                                   | Package                                 | Description                                                                                                                                                                                    |
|----------------------------------------|-----------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `GlobalExceptionHandler.java`          | `com.autodev.platformservice.exception` | `@RestControllerAdvice` handling: `UserAlreadyExistsException` → 409, `RegistrationOperationException` → 502, `KeycloakInfrastructureException` → 503, `MethodArgumentNotValidException` → 400 |
| `ErrorResponse.java`                   | `com.autodev.platformservice.exception` | Record: status, error, message, path, timestamp                                                                                                                                                |
| `FieldViolation.java`                  | `com.autodev.platformservice.exception` | Record: field, message                                                                                                                                                                         |
| `UserAlreadyExistsException.java`      | `com.autodev.platformservice.exception` | Extends `RuntimeException`                                                                                                                                                                     |
| `RegistrationOperationException.java`  | `com.autodev.platformservice.exception` | Extends `RuntimeException`                                                                                                                                                                     |
| `KeycloakInfrastructureException.java` | `com.autodev.platformservice.exception` | Extends `RuntimeException`                                                                                                                                                                     |

---

## Known Incomplete / Placeholder Code

| Location                                     | Issue                                                                                                                                   |
|----------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------|
| `RegistrationService.registerUser()`         | Method body is a skeleton — Keycloak admin calls and outbox event creation are not fully implemented                                    |
| `UserProfileService.getCurrentUserProfile()` | Returns `null` unconditionally                                                                                                          |
| `UserProfileService.updateUserProfile()`     | Method is a skeleton (no implementation)                                                                                                |
| `UserProfileService.getUserProfileById()`    | Only partially implemented (repository exists but mapping may be incomplete)                                                            |
| `ReviewRepository`                           | ⚠️ Does NOT extend `JpaRepository`, `CrudRepository`, or any Spring Data interface — queries are defined but the bean cannot be created |
| `platform-service` controllers               | **No controllers exist at all** — no REST endpoints are exposed internally (only gateway routes to the service exist)                   |
| `services/catalog-service/`                  | Empty directory with only `build.gradle.kts`                                                                                            |
| `services/order-service/`                    | Empty directory with only `build.gradle.kts`                                                                                            |
| `services/payment-service/`                  | Empty directory with only `build.gradle.kts`                                                                                            |
| `services/communication-service/`            | Empty directory with only `build.gradle.kts`                                                                                            |
| `services/notification-service/`             | Empty directory with only `build.gradle.kts`                                                                                            |

---

## Test Landscape

### Test Frameworks

- **Unit Tests**: JUnit 5 + Mockito
- **Integration Tests**: Testcontainers + WireMock + Reactor Test
- **Build Tool**: Gradle with custom `integrationTest` task

### Test Configuration

#### Unit Tests

- **Task**: `test`
- **Framework**: JUnit Platform
- **Runner**: JUnit Jupiter

#### Integration Tests

- **Task**: `integrationTest`
- **Timeout**: 10 minutes
- **Coverage**: Depends on `test` task
- **Report Format**: HTML + JUnit XML (for GitLab CI)

#### Coverage Reports

- **Task**: `jacocoAggregatedReport`
- **Output**: XML (GitLab), HTML
- **Scope**: Aggregated across all subprojects

### Test Coverage by Service

| Service            | Unit Tests | Integration Tests                          |
|--------------------|------------|--------------------------------------------|
| `api-gateway`      | None       | 7 test classes (Testcontainers + WireMock) |
| `platform-service` | None       | None                                       |
| All other services | None       | None                                       |

### Test Directories

```
services/api-gateway/src/integration-test/java/
services/api-gateway/src/integration-test/resources/
services/platform-service/src/integration-test/java/
services/platform-service/src/integration-test/resources/
```

### Running Tests

```bash
# Run all tests
./gradlew test

# Run integration tests
./gradlew integrationTest

# Run all tests with coverage
./gradlew test jacocoAggregatedReport

# Run all checks
./gradlew check
```

---

## Database Migrations

### Database Type

- **Type**: PostgreSQL 15
- **Migration Tool**: Liquibase 4.27.0
- **Schema Strategy**: Multi-schema per service

### Schema Structure

| Schema          | Purpose                               | Status         |
|-----------------|---------------------------------------|----------------|
| `platform`      | User profiles, reviews, outbox events | ✅ Implemented  |
| `catalog`       | Product catalog data                  | 📋 Placeholder |
| `orders`        | Order management                      | 📋 Placeholder |
| `payment`       | Payment processing                    | 📋 Placeholder |
| `communication` | Communication logs                    | 📋 Placeholder |
| `notification`  | Notification queue                    | 📋 Placeholder |

### Implemented Migrations (platform schema, in order)

#### Tables

1. **platform.user_profiles**
    - File: `07-06-2026-create-table-user-profiles.sql`
    - Fields: id (UUID, PK), keycloak_user_id (VARCHAR, UNIQUE, NOT NULL), store_name (VARCHAR), store_description (
      TEXT), store_logo_url (VARCHAR), avatar_url (VARCHAR), verification_status (VARCHAR), loyalty_balance (INTEGER,
      DEFAULT 0), created_at (TIMESTAMP), updated_at (TIMESTAMP)

2. **platform.outbox_events**
    - File: `20-07-2026-create-table-outbox.sql`
    - Fields: id (UUID, PK), aggregate_type (VARCHAR, NOT NULL), aggregate_id (VARCHAR, NOT NULL), event_type (VARCHAR,
      NOT NULL), topic (VARCHAR, NOT NULL), payload (JSONB, NOT NULL), status (VARCHAR, NOT NULL, DEFAULT 'PENDING'),
      created_at (TIMESTAMP, NOT NULL)

3. **platform.reviews**
    - File: `20-07-2026-create-table-reviews.sql`
    - Fields: id (UUID, PK), product_id (VARCHAR, NOT NULL), user_id (VARCHAR, NOT NULL), rating (INTEGER),
      review_text (TEXT), seller_reply (TEXT), created_at (TIMESTAMP), updated_at (TIMESTAMP)

4. **platform.user_profiles (ALTER)**
    - File: `25-07-2026-add-email-to-user-profiles.sql`
    - Change: ADD COLUMN email VARCHAR(255), ADD UNIQUE constraint on email

#### Indexes

1. `platform.user_profiles-store_name` — B-tree on store_name
2. `platform.user_profiles-verification_status` — B-tree on verification_status
3. `platform.outbox_events-status_created_at` — B-tree on status, created_at
4. `platform.reviews-product_id` — B-tree on product_id
5. `platform.reviews-user_id` — B-tree on user_id

### Migration Files Location

```
services/platform-service/src/main/resources/db/changelog/
├── master.yaml                  # Master changelog (includes v1.0.0 directory)
└── v1.0.0/                      # Version 1.0.0 migrations (9 files)
    ├── 07-06-2026-create-table-user-profiles.sql
    ├── 07-06-2026-create-index-platform-service.user-profiles-store.sql
    ├── 07-06-2026-create-index-platform-service.user-profiles-verification.sql
    ├── 20-07-2026-create-table-outbox.sql
    ├── 20-07-2026-create-table-reviews.sql
    ├── 20-07-2026-create-index-platform.outbox-events-status-created-at.sql
    ├── 20-07-2026-create-index-platform.reviews-product-id.sql
    ├── 20-07-2026-create-index-platform.reviews-user-id.sql
    └── 25-07-2026-add-email-to-user-profiles.sql
```

### Database Initialization

- **Initial Script**: `infrastructure/service-db/init/init-schemas.sql`
- **Purpose**: Creates all 6 schema namespaces (platform, catalog, orders, payment, communication, notification) on
  container startup

---

## Infrastructure Configuration

### Docker Compose Files

| File                                           | Purpose                                            | Services Count                                                       |
|------------------------------------------------|----------------------------------------------------|----------------------------------------------------------------------|
| `docker-compose.yaml`                          | Full development stack (infra + app commented out) | 14 infrastructure services                                           |
| `docker-compose-infra.yaml`                    | Minimal infrastructure                             | consul, services-database, keycloak, keycloak-database, minio, redis |
| `docker-compose-infra-with-observability.yaml` | Full observability stack                           | Adds prometheus, loki, tempo, alloy, grafana, elasticsearch, kafka   |

### Infrastructure Components

| Service           | Image                     | Version | Port(s)                    | Purpose                                      |
|-------------------|---------------------------|---------|----------------------------|----------------------------------------------|
| consul            | consul                    | 1.15.3  | 8500                       | Service Discovery / Registry                 |
| services-database | postgres                  | 15      | 5438                       | Main application database                    |
| keycloak-database | postgres                  | 15      | 5435                       | Keycloak identity database                   |
| keycloak          | quay.io/keycloak/keycloak | 21.1.1  | 8090                       | Identity & Access Management (OIDC/OAuth2)   |
| minio             | minio/minio               | latest  | 9000 (API), 9001 (Console) | S3-compatible object storage                 |
| redis             | redis                     | 7       | 6379                       | Caching & rate limiting backend              |
| kafka             | apache/kafka              | 4.2.0   | 9092, 29092                | Event streaming / message broker             |
| elasticsearch     | elasticsearch             | 8.13.0  | 9200                       | Search & indexing                            |
| prometheus        | prom/prometheus           | v3.9.1  | 9091                       | Metrics collection                           |
| loki              | grafana/loki              | main    | 3100                       | Log aggregation                              |
| tempo             | grafana/tempo             | 2.4.1   | 3200                       | Distributed tracing                          |
| alloy             | grafana/alloy             | v1.12.2 | 9080, 4318                 | OpenTelemetry collector / telemetry pipeline |
| grafana           | grafana/grafana           | 12.4.0  | 3000                       | Metrics visualization & dashboards           |

### Keycloak Realm Configuration

- **Realm file**: `infrastructure/keycloak/realms/autodev-realm.json`
- **Realm name**: autodev
- **Client**: `autodev-marketplace` (confidential, client-secret auth)
- **Roles**: ADMIN, SELLER, BUYER
- **Users**: admin (ADMIN), seller1 (SELLER), buyer1 (BUYER) — all with password "password"

### Monitoring Configuration

| Component  | Config File(s)                                                                                | Key Settings                                                         |
|------------|-----------------------------------------------------------------------------------------------|----------------------------------------------------------------------|
| Prometheus | `infrastructure/monitoring/prometheus.yml`                                                    | Scrapes alloy at localhost:9080, consul SD                           |
| Loki       | `infrastructure/monitoring/loki-config.yaml`                                                  | Local filesystem storage, no auth                                    |
| Tempo      | `infrastructure/monitoring/tempo-config.yaml`                                                 | Local backend, OTLP gRPC on 4317, OTLP HTTP on 4318                  |
| Alloy      | `infrastructure/monitoring/config.alloy`                                                      | Receives OTLP logs/metrics/traces, forwards to Prometheus/Loki/Tempo |
| Grafana    | `infrastructure/monitoring/dashboards/`, `infrastructure/monitoring/grafana-datasources.yaml` | Pre-configured Prometheus, Loki, Tempo datasources                   |

---

## Entry Points & Configuration

### Main Application Classes

| Service               | Main Class                   | Package                       |
|-----------------------|------------------------------|-------------------------------|
| api-gateway           | `GatewayApplication`         | `com.autodev.gateway`         |
| platform-service      | `PlatformServiceApplication` | `com.autodev.platformservice` |
| catalog-service       | *Not yet created*            | *To be determined*            |
| order-service         | *Not yet created*            | *To be determined*            |
| payment-service       | *Not yet created*            | *To be determined*            |
| communication-service | *Not yet created*            | *To be determined*            |
| notification-service  | *Not yet created*            | *To be determined*            |

### Docker Configuration

#### Dockerfiles

- **api-gateway**: `services/api-gateway/Dockerfile`
    - Multi-stage build with Gradle 8.14.5
    - Uses BellSoft Liberica JDK 17
    - Spring Boot Layered JAR approach
    - Exposes port 8080
- **Other services**: No Dockerfiles exist yet

### Configuration Files

#### api-gateway

- **Primary**: `services/api-gateway/src/main/resources/application.yml`
- **Profile**: `docker` (for containerized deployment)
- **Key Configs**: Redis, OAuth2 JWT, Gateway routes, Consul, Resilience4j

#### platform-service

- **Primary**: `services/platform-service/src/main/resources/application.yml`
- **Key Configs**: PostgreSQL datasource, JPA, Redis, Kafka, Consul, Resilience4j

---

## Continuous Integration

### GitLab CI (`.gitlab-ci.yml`)

- **Image**: `bellsoft/liberica-openjdk-debian:17`
- **Stages**: build → test → integration-test → coverage → mr-check
- **Integration Tests**: Use Testcontainers (Docker-in-Docker service)
- **Coverage**: JaCoCo XML → GitLab MR comment visualization
- **MR Quality Gate**: GitLab API call to set pipeline as MR approval
- **Artifacts**: build libs, integration test reports, coverage reports
- **Rules**: MRs run full pipeline; main branch runs build+test only

### GitHub Actions (`.github/workflows/gradle-ci.yaml`)

- **Triggers**: push, pull_request on main branch
- **Steps**: Checkout → JDK 17 → Gradle build (with caching) → Upload build artifacts
- **Note**: Simpler than GitLab CI — no integration tests or coverage gates

---

## Environment Variables

| Variable                  | Purpose         | Default Value                        |
|---------------------------|-----------------|--------------------------------------|
| `SERVER_PORT`             | Service port    | 8081 (platform)                      |
| `PLATFORM_DB_HOST`        | DB host         | localhost                            |
| `PLATFORM_DB_PORT`        | DB port         | 5438                                 |
| `PLATFORM_DB_NAME`        | DB name         | services                             |
| `PLATFORM_DB_USER`        | DB user         | postgres                             |
| `PLATFORM_DB_PASS`        | DB password     | postgres                             |
| `REDIS_HOST`              | Redis host      | localhost                            |
| `REDIS_PORT`              | Redis port      | 6379                                 |
| `REDIS_PASSWORD`          | Redis password  | redis                                |
| `CONSUL_HOST`             | Consul host     | localhost                            |
| `CONSUL_PORT`             | Consul port     | 8500                                 |
| `KEYCLOAK_ISSUER_URI`     | Keycloak issuer | http://localhost:8090/realms/autodev |
| `KAFKA_BOOTSTRAP_SERVERS` | Kafka servers   | localhost:9092                       |

---

## Architecture Patterns

1. **Multi-Database Architecture**: Each service may have its own database schema or instance
2. **Event-Driven Communication**: Kafka for inter-service messaging (outbox pattern)
3. **API Gateway Pattern**: Centralized routing, rate limiting, JWT validation
4. **Circuit Breaker**: Resilience4j for fault tolerance
5. **Observability Stack**: Prometheus + Grafana + Loki + Tempo (OpenTelemetry)
6. **Service Discovery**: Consul for dynamic service registration

## Security

- **Authentication**: Keycloak 21.1.1 (OIDC/OAuth2)
- **JWT Validation**: Resource server configuration with custom role extraction
- **Rate Limiting**: Per-IP rate limiting via Resilience4j Redis
- **CORS**: Whitelist of allowed origins (api-gateway configured)
- **Roles**: ADMIN, SELLER, BUYER (defined in Keycloak realm)

## Build System

- **DSL**: Kotlin (`.kts`)
- **Version Management**: Centralized in `gradle/libs.versions.toml`
- **Plugin Management**: `pluginManagement` block in `settings.gradle.kts`
- **Source Sets**: Custom `integrationTest` source set per service

## Coding Standards

- **Java Version**: 17
- **Package Naming**: `com.autodev.{service}`
- **Entity Package**: `com.autodev.{service}.entity`
- **Repository Package**: `com.autodev.{service}.repository`
- **Config Package**: `com.autodev.{service}.config`
- **Security Package**: `com.autodev.{service}.security`

## Development Notes

1. **Empty Services**: catalog-service, order-service, payment-service, communication-service, notification-service are
   placeholders with only `build.gradle.kts`
2. **Active Development**: api-gateway and platform-service are actively developed
3. **Database Schema**: `platform` schema is actively developed (9 migration files, 4 tables)
4. **Outbox Pattern**: Implemented for reliable event publishing (but service logic is incomplete)
5. **Docker Profile**: Use `docker` profile for containerized deployments
6. **Missing Controllers**: platform-service has no REST controllers — the service cannot serve any HTTP requests yet
7. **Broken Repository**: `ReviewRepository` does not extend a Spring Data parent interface and will fail at startup

## Common Commands

```bash
# Build all modules
./gradlew build

# Build without tests
./gradlew build -x test

# Run all tests
./gradlew test

# Run integration tests
./gradlew integrationTest

# Run all checks
./gradlew check

# Generate coverage report
./gradlew jacocoAggregatedReport

# Run specific service
./gradlew :services:api-gateway:bootRun
./gradlew :services:platform-service:bootRun

# Docker build
./gradlew :services:api-gateway:bootJar

# Start infrastructure
docker-compose up -d

# Start all services
docker-compose up -d --build
```

---

*This file was automatically generated to provide comprehensive project documentation for AI agents. Last updated:
2026-07-30.*