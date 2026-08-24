# Project Overview: autodev-marketplace

# AutoDev Marketplace — Обзор проекта

**AutoDev Marketplace** — это учебная платформа для продажи автозапчастей, построенная по принципам современных распределённых систем. 
Проект предназначен для обучения Java backend-разработчиков best practices разработки микросервисных приложений на Spring Boot 3.x и Java 17+.
ИИ-агент выполняет функцию ментора, он должен помогать осваивать методы разработки данного приложения с учетом лучших практик разработки приложения и лучших практик построения процесса разработки:
в том числе минимизации размеров пулл реквеста, настройки CI/CD пайплайна с использованием различных линтеров и валидаторов, соблюдением требований к развертыванию приложений, декомпозиции фичей на задачи, с формированием задач в проекте github.
ИИ-агент как старший, более опытный специалист должен следить за соблюдением лучших практик, помогать в декомпозиции задач, наставлять разработчика при нарушении им практик разработки проекта в условиях, максимально приближенных к реальному процессу разработки проекта в команде
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
| **Управление объявлениями** | 4 функции | listing-service |
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
| | Email, SMS уравомещения | |

### Что включено в MVP:
- 8 микросервисов с базовой функциональностью
- Direct Keycloak Integration для аутентификации (без auth-service)
- PostgreSQL FTS для поиска (без search-service)
- Упрощённая схема оплаты без эскроу и интеграций с банками
- Базовый чат без шаблонов и истории переписки
- Справочник запчастей и объявлений разделен на два независимых сервиса (Read/Write)

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

### MVP-состав (8 сервисов)

| Сервис | Обязательный             | Описание |
|--------|--------------------------|----------|
| **api-gateway** | ✅ Implemented            | Spring Cloud Gateway с reactive stack, rate limiting, JWT validation |
| **platform-service** | ✅ Partially Implemented  | User profiles, reviews, outbox pattern, REST controllers, unit & integration tests  |
| **catalog-service** | ⚠️ In Progress           | Справочник запчастей, категорий, поиск по VIN/артикулу (Read-Only). Начат слой JPA-сущностей (`AbstractBaseEntity`, `CategoryEntity`) |
| **listing-service** | 📋 Scaffold (WIP)         | Объявления продавцов, CRUD-операции, статусы (Write-Heavy). Создан каркас сервиса и маршрут в gateway |
| **order-service** | 📋 Empty                 | Корзина, оформление заказов, управление заказами |
| **payment-service** | 📋 Empty                 | Обработка платежей (базовая, без эскроу и интеграций) |
| **communication-service** | 📋 Empty                 | Чат в реальном времени (базовый функционал) |
| **notification-service** | 📋 Empty                 | Email, SMS уведомления |

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

The project consists of 8 microservices:

| Module                           | Status                   | Description                                                             | Port |
|----------------------------------|--------------------------|-------------------------------------------------------------------------|------|
| `services:api-gateway`           | ✅ Implemented            | Spring Cloud Gateway with reactive stack, rate limiting, JWT validation | 8080 |
| `services:platform-service`      | ⚠️ Partially Implemented | User profiles, reviews, outbox pattern (Spring Data JPA, Kafka) | 8081 |
| `services:catalog-service`       | ⚠️ In Progress           | Справочник запчастей, категорий, поиск по VIN/артикулу (Read-Only). Начат слой JPA-сущностей | -    |
| `services:listing-service`     | 📋 Scaffold (WIP)         | Объявления продавцов, CRUD-операции, статусы (Write-Heavy). Каркас сервиса + маршрут в gateway | -    |
| `services:order-service`         | 📋 Empty                 | Корзина, оформление заказов, управление заказами | -    |
| `services:payment-service`     | 📋 Empty                 | Обработка платежей (базовая, без эскроу и интеграций) | -    |
| `services:communication-service` | 📋 Empty                 | Чат в реальном времени (базовый функционал) | -    |
| `services:notification-service`  | 📋 Empty                 | Email, SMS уведомления | -    |

---

## Dependency Manifest

### Version Catalog (`gradle/libs.versions.toml`)

**Versions:**
| Alias | Version |
|-------|----------|
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
| `jetty-server` | 11.0.24 (для тестов WebMvcTest) |
|--------|----------|

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
| spring-cloud-loadbalancer  | `spring-cloud-loadbalancer`                                 |
| spring-cloud-stubrunner    | `spring-cloud-contract-stub-runner`                         |
| postgres                   | `org.postgresql:postgresql:42.7.7`                            |
| liquibase                  | `org.liquibase:liquibase-core:4.27.0`                         |
| resilence4j-ratelimiter    | `io.github.resilience4j:resilience4j-ratelimiter:2.2.0` |
| resilence4j-core           | `io.github.resilience4j:`
| resilence4j-micrometer     | `io.github.resilience4j:resilience4j-micrometer`                |
| resilence4j-spring-boot3   | `io.github.resilience4j:resilience4j-spring-boot3`  |
| micrometer-prometheus      | `io.micrometer:micrometer-registry-prometheus`                        |
| micrometer-tracing-bridge  | `io.micrometer:micrometer-tracing-bridge-otel`                 |
| opentelemetry-exporter     | `io.opentelemetry:opentelemetry-exporter-otlp`                 |
| lombok                     | `org.projectlombok:lombok:1.18.46`                            |
| lombok-mapstruct-binding   | `org.projectlombok:lombok-mapstruct-binding:0.2.0`                    |
| mapstruct                  | `org.mapstruct:mapstruct:1.6.3`                               |
| mapstruct-processor        | `org.mapstruct:mapstruct-processor:1.6.3`                    |
| apache-commons-pool        | `org.apache.commons:commons-pool2:2.13.0`                     |

**Test Libraries:**

| Alias                     | Maven Coordinates                                                            |
|---------------------------|-----------------------------------------------------------------------|
| junit-jupiter             | `org.junit.jupiter:junit-jupiter`                                            |
| nimbus-jwt                | `com.nimbusds:nimbus-jose-jwt:9.31`                                          |
| mockito-junit             | `org.mockito:mockito-junit-jupiter:5.11.0`                                   |
| reactor-test              | `io.projectreactor:reactor-test:3.8.0`                                       |
| redis-testcontainers      | `org.testcontainers:testcontainers:2.0.5` + `com.redis:testcontainers-redis` |
| testcontainers            | `org.testcontainers:testcontainers:2.0.5` |
| testcontainers-junit      | `org.testcontainers:junit-jupiter:2.0.5`                                     |
| testcontainers-kafka      | `org.testcontainers:kafka:2.0.5` |
| testcontainers-postgresql | `org.testcontainers:postgresql:2.0.5` |
| wiremock                  | `org.wiremock:wiremock:3.9.2` |
| wiremock-spring           | `org.wiremock.integrations:wiremock-spring-boot:3.9.2` |
| spring-boot-test          | `org.springframework.boot:spring-boot-starter-test`                          |
| jetty-server             | `org.eclipse.jetty:jetty-server:11.0.24`                            |

---

## Implemented Classes (Full Inventory)

### api-gateway (`com.autodev.gateway`)

**Source Files:**

| File                                              | Package               | Description                                                                                           |
|---------------------------------------------------|-----------------------|-------------------------------------------------------------------------------------------------------|
| `GatewayApplication.java`                         | `com.autodev.gateway` | Main Spring Boot application class                                                                    |
| `SecurityConfig.java`                             | `com.autodev.gateway.config` | Security configuration: CORS whitelist, OAuth2 resource server with JWT, disabled CSRF                |
| `RateLimitingFilter.java`                         | `com.autodev.gateway.filter` | Reactive rate-limiting filter per IP using Redis + Resilience4j                                       |
| `RateLimitingProperties.java`                     | `com.autodev.gateway.config` | Configuration properties for rate limiting (limit-for-period, limit-refresh-period, timeout-duration) |
| `KeycloakReactiveJwtAuthenticationConverter.java` | `com.autodev.gateway.security` | Custom JWT authentication converter extracting roles from Keycloak tokens                             |

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
|----------------------------|------------------------|--------------|---------------|
| `/api/v1/platform/**`      | platform-service      | ALL          | Yes           |
| `/api/v1/catalog/**`       | catalog-service       | ALL          | Yes           |
| `/api/v1/listings/**`      | listing-service     | ALL          | Yes           |
| `/api/v1/orders/**`        | order-service       | ALL          | Yes           |
| `/api/v1/payments/**`      | payment-service     | ALL          | Yes           |
| `/api/v1/communication/**` | communication-service | ALL          | Yes           |
| `/api/v1/notifications/**`  | notification-service  | ALL          | Yes           |
| `/actuator/**`             | api-gateway (local) | ALL          | No            |


Note: No controllers exist yet in any backend service. All routes return 503 (WireMock stubs in tests) or will forward to Consul-discovered services.

### platform-service (`com.autodev.platformservice`)

**Entities:**

| File                      | Package                              | Description                                                                                                                                                                                                |
|---------------------------|--------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------|
| `AbstractBaseEntity.java` | `com.autodev.platformservice.entity` | Abstract base: `@MappedSuperclass` with `@Id`, `@CreatedDate`, `@LastModifiedDate`, `@Version`                                             |
| `UserProfileEntity.java`  | `com.autodev.platformservice.entity` | JPA entity for `platform.user_profiles`. Fields: id, keycloakUserId (unique String), storeName, storeDescription, storeLogoUrl, avatarUrl, verificationStatus (enum), loyaltyBalance (int), email, createdAt, updatedAt |
| `ReviewEntity.java`       | `com.autodev.platformservice.entity` | JPA entity for `platform.reviews`. Fields: id, productId (String), userId (String), rating (int), reviewText (TEXT), sellerReply (TEXT), createdAt, updatedAt                    |
| `OutboxEntity.java`       | `com.autodev.platformservice.entity` | JPA entity for `platform.outbox_events`. Fields: id, aggregateType, aggregateId, eventType, topic, payload (JSONB), status (enum), createdAt (Timestamp)              |
| `DomainEvent.java`        | `com.autodev.platformservice.entity` | Интерфейс доменного события для Outbox                                                                                                               | 
| `OutboxStatus.java`       | `com.autodev.platformservice.entity` | Enum: PENDING, SENT, FAILED                                                                                                                |
| `UserEvents.java`         | `com.autodev.platformservice.entity` | Enum: USER_REGISTERED, USER_PROFILE_UPDATED, USER_VERIFIED                                                 |
| `VerificationStatus.java` | `com.autodev.platformservice.entity` | Enum: UNVERIFIED, VERIFIED, BANNED                                                                         |

**Repositories:**

| File                         | Package                                  | Description                                                                                                                                                                                                                      |
|------------------------------|------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `UserProfileRepository.java` | `com.autodev.platformservice.repository` | `JpaRepository<UserProfileEntity, UUID>`. Custom: `findByKeycloakUserId(String)`, `existsByKeycloakUserId(String)`, `existsByStoreName(String)`                                                                                  |
| `OutboxRepository.java`      | `com.autodev.platformservice.repository` | `JpaRepository<OutboxEntity, UUID>`. Custom: `findByStatusOrderByCreatedAtAsc(OutboxStatus)`                                                                                     |
| `ReviewRepository.java`      | `com.autodev.platformservice.repository` | `JpaRepository<ReviewEntity, UUID>`. Custom: `findByProductId(String)`, `findByUserId(String)`, `findBySellerReplyIsNull()`                                                                                 |

**DTOs:**

| File                           | Package                           | Description                                                                                                                               |
|--------------------------------|-----------------------------------|---------------------------------------------------------------------------------------------------------------------------------------|
| `RegisterRequestDto.java`      | `com.autodev.platformservice.dto` | Registration input: username, email, password, storeName, storeDescription                                                                |
| `UpdateProfileRequestDto.java` | `com.autodev.platformservice.dto` | Profile update input: storeName, storeDescription, storeLogoUrl, avatarUrl                                                                |
| `UserRegisteredEventPayload.java` | `com.autodev.platformservice.dto.event` | Payload события USER_REGISTERED                                                                                                                 |
| `UserProfileResponseDto.java`  | `com.autodev.platformservice.dto` | Profile output: id, keycloakUserId, storeName, storeDescription, storeLogoUrl, avatarUrl, verificationStatus, email, createdAt, updatedAt |

**Mappers:**

| File                     | Package                              | Description                                                                                       |
|--------------------------|--------------------------------------|-------------------------------------------------------------------------------------------------|
| `UserProfileMapper.java` | `com.autodev.platformservice.mapper` | MapStruct: `RegisterRequestDto → UserProfileEntity`, `UserProfileEntity → UserProfileResponseDto`             |
| `UpdateProfileMapper.java` | `com.autodev.platformservice.mapper` | MapStruct: `UpdateProfileRequestDto → @MappingTarget UserProfileEntity` (игнорируются системные поля: id, email, статус) |

**Services:**

| File                       | Package                               | Description                                                                                                                                                                                                                                            |
|----------------------------|---------------------------------------|-------------------------------------------------------------------------------------------------------------------------------|
| `RegistrationService.java` | `com.autodev.platformservice.service` | Регистрация пользователя: Keycloak Admin API → БД (транзакция) → Outbox → Compensating Transaction (откат в Keycloak при падении БД)                             |
| `UserProfileService.java`  | `com.autodev.platformservice.service` | Получение профиля (с Self-Healing при отсутствии), обновление профиля, получение профиля по ID                                      |
| `OutboxService.java`       | `com.autodev.platformservice.service` | Публикация доменных событий в Outbox (сериализация в JSON, сохранение со статусом PENDING)                           |

**Controllers:**

| File                     | Package                                  | Description                                                                                                                                      |
|--------------------------|------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------|
| `RegistrationController.java` | `com.autodev.platformservice.controller` | Контроллер регистрации: `POST /api/v1/platform/registration`                                   |
| `UserProfileController.java` | `com.autodev.platformservice.controller` | Контроллер профилей пользователей: `GET/PUT /api/v1/platform/profile`  |


**Clients:**

| File                           | Package                                       | Description                                                                                                                                           |
|--------------------------------|-----------------------------------------------|-------------------------------------------------------------------------------------------|
| `KeycloakAdminClient.java`     | `com.autodev.platformservice.client.keycloak` | REST-клиент для управления пользователями в Keycloak (создание, удаление через Admin API)                      |
| `KeycloakClientConfig.java`    | `com.autodev.platformservice.client.keycloak` | Конфигурация для Keycloak client: base URL, realm, admin credentials                                                                                 |
| `KeycloakAuthInterceptor.java` | `com.autodev.platformservice.client.keycloak` | `ClientHttpRequestInterceptor` добавляет `Authorization: Bearer` header                           |
| `KeycloakTokenProvider.java`   | `com.autodev.platformservice.client.keycloak` | Управление токеном Keycloak Admin Client (кэширование credentials grant token с тайма жизни)                           |
| `KeycloakTokenResponse.java`   | `com.autodev.platformservice.client.keycloak` | Record: access_token, expires_in, refresh_expires_in, token_type, not-before-policy, scope                            |
| `KeycloakClientLoggingUtils.java` | `com.autodev.platformservice.client.keycloak` | Утилиты для безопасного логирования ответов Keycloak (ограничение тела ответа без риска утечки данных)                         |

**Configuration:**

| File                      | Package                              | Description                                                                                                       |
|---------------------------|--------------------------------------|-----------------------------------------------------------------------------------------------------------------------|
| `SecurityConfig.java`     | `com.autodev.platformservice.config` | Security config: OAuth2 resource server, role-based access (ADMIN, SELLER, BUYER), CORS, disabled CSRF   |
| `KeycloakProperties.java` | `com.autodev.platformservice.config` | `@ConfigurationProperties(prefix = "keycloak.admin")`: baseUrl, realm, username, password, clientId, clientSecret                         |

**Security Utilities:**

| File                                      | Package                                | Description                                                                              |
|-------------------------------------------|----------------------------------------|--------------------------------------------------------------------------------|
| `SecurityUtils.java`                      | `com.autodev.platformservice.security` | Static helper: extract current user's Keycloak ID and roles from `SecurityContextHolder` |
| `KeycloakJwtAuthenticationConverter.java` | `com.autodev.platformservice.security` | Extracts roles and Keycloak user ID from JWT claims into `GrantedAuthority` list             |

**Exception Handling:**

| File                                   | Package                                 | Description                                                                                                                                                                                    |
|-------------------------------------------|-----------------------------------------|------------------------------------------------------------------------------------------------------------------------------|
| `GlobalExceptionHandler.java`          | `com.autodev.platformservice.exception` | `@RestControllerAdvice` handling: `UserAlreadyExistsException` → 409, `RegistrationOperationException` → 500, `KeycloakInfrastructureException` → 503, `MethodArgumentNotValidException` → 400 |
| `ErrorResponse.java`                   | `com.autodev.platformservice.exception` | Record: status, error, message, path, timestamp                                                                 |
| `FieldViolation.java`                  | `com.autodev.platformservice.exception` | Record: field, message                                                                                   |
| `UserAlreadyExistsException.java`      | `com.autodev.platformservice.exception` | Extends `RuntimeException`                                                                                     |
| `RegistrationOperationException.java`  | `com.autodev.platformservice.exception` | Extends `RuntimeException` с конструктором `(String message, Throwable cause)`                                             |
| `KeycloakInfrastructureException.java` | `com.autodev.platformservice.exception` | Extends `RuntimeException`                                                                                     |
| `UserProfileNotFoundException.java`  | `com.autodev.platformservice.exception` | Extends `RuntimeException` (будет использоваться в `listing-service` при поиске объявлений)                                                 |

### catalog-service (`com.autodev.catalogservice`) — In Progress

Начат слой JPA-сущностей (стек JPA: `jakarta.persistence` + Hibernate, аналогично platform-service):

| File                      | Package                              | Description                                                                                                                                                                                                |
|---------------------------|--------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------|
| `AbstractBaseEntity.java` | `com.autodev.catalogservice.entity`  | `@MappedSuperclass`: `@Id` UUID (`@UuidGenerator`), `createdAt`/`updatedAt` (`OffsetDateTime`, `@CreationTimestamp`/`@UpdateTimestamp`). Отличается от platform-версии: нет `@Version` и JPA-auditing, используются Hibernate-аннотации времени |
| `CategoryEntity.java`     | `com.autodev.catalogservice.entity`  | `@Entity` `@Table(name = "category")` extends `AbstractBaseEntity`. Пока без полей (каркас сущности категории)                                  |

> Отсутствуют: `application.yml`, репозитории, DTO, сервисы, контроллеры, миграции для схемы `catalog`.

---

## Known Incomplete / Placeholder Code

> ⚠️ **Текущая работа (ветка `feature/listing-service/create-service`):** создаётся каркас `listing-service` (пустой `build.gradle.kts`), в `catalog-service` добавлен начальный слой JPA-сущностей (`AbstractBaseEntity`, `CategoryEntity`), в gateway добавлен маршрут `/api/v1/listings/**`, в `init-schemas.sql` добавлена схема `listings`.

| Location                         | Issue                                                                                                                                   |
|----------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------|
| `services/catalog-service/`           | Начат слой сущностей, но нет `application.yml`, DTO/репозиториев/контроллеров; `build.gradle.kts` пустой (0 строк)              |
| `services/listing-service/`         | Каркас сервиса создан (ветка WIP); `build.gradle.kts` пустой (0 строк), нет `application.yml` и кода                                |
| `services/order-service/`             | 📋 Empty directory: только пустой `build.gradle.kts` (0 строк)                                                  |
| `services/payment-service/`           | 📋 Empty directory: только пустой `build.gradle.kts` (0 строк)                                                        |
| `services/communication-service/`     | 📋 Empty directory: только пустой `build.gradle.kts` (0 строк)                                                 |
| `services/notification-service/`      | 📋 Empty directory: только пустой `build.gradle.kts` (0 строк)                                                 |

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
- **Runner:** JUnit Jupiter

#### Integration Tests
- **Task**: `integrationTest`
- **Timeout**: 10 minutes
- **Coverage**: Depends on `test` task
- **Report Format:** HTML + JUnit XML (для GitLab CI)

### Test Coverage by Service

| Service            | Unit Tests | Integration Tests                          |
|--------------------|------------|--------------------------------------------|
| `api-gateway`      | None       | 7 test classes (Testcontainers + WireMock) |
| `platform-service` | 2 класса (RegistrationService, UserProfileService) | 4 файла: `RegistrationControllerTest`, `RegistrationServiceIntegrationTest` + базовые `AbstractPlatformIntegrationTest`, `AbstractControllerTest` |
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
- **Migration Tool**: Liquibase 4.27. `private UUID id = UUID.randomUUID();`
- **Schema Strategy**: Multi-schema per service

### Schema Structure

| Schema          | Purpose                               | Status         |
|-----------------|---------------------------------------|----------------|
| `platform`      | User profiles, reviews, outbox events | ✅ Implemented  |
| `catalog`       | Справочник запчастей, категорий, поиск | ⚠️ Schema создана; начат слой сущностей (миграций/таблиц пока нет) |
| `listings`      | Объявления продавцов, статусы заказов        | ⚠️ Schema создана (`init-schemas.sql`); таблиц и миграций пока нет |
| `orders`        | Заказы, доставка, возвраты                      | 📋 Placeholder |
| `payment`       | Платежи                               | 📋 Placeholder |
| `communication` | Логи чата                               | 📋 Placeholder |
| `notification`  | Очередь                                 | 📋 Placeholder |

### Implemented Migrations (platform schema, in order)

#### Tables

1. **platform.user_profiles**
   - File: `07-06-2026-create-table-user-profiles.sql`
   - Fields: id (UUID, PK), keycloak_user_id (VARCHAR, UNIQUE, NOT NULL), store_name, store_description, store_logo_url, avatar_url, verification_status (enum), loyalty_balance (INTEGER, DEFAULT 0), created_at, updated_at

2. **platform.outbox_events**
   - File: `20-07-2026-create-table-outbox.sql`
   - Fields: id (UUID, PK), aggregate_type (VARCHAR, NOT NULL), aggregate_id (VARCHAR, NOT NULL), event_type (VARCHAR, NOT NULL), topic (VARCHAR, NOT NULL), payload (JSONB, NOT NULL), status (VARCHAR, NOT NULL, DEFAULT 'PENDING'), created_at (TIMESTAMP, NOT NULL)

3. **platform.reviews**
   - File: `20-07-2026-create-table-reviews.sql`
   - Fields: id (UUID, PK), product_id (VARCHAR, NOT NULL), user_id (VARCHAR, NOT NULL), rating (INTEGER), review_text (TEXT), seller_reply (TEXT), created_at (TIMESTAMP), updated_at (TIMESTAMP)

4. **platform.user_profiles (ALTER)**
   - File: `25-07-2026-add-email-to-user-profiles.sql`
   - Change: ADD COLUMN email VARCHAR(255), ADD UNIQUE constraint on email

### Indexes

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
- **Purpose:** Creates all 7 schema namespaces (platform, catalog, listings, orders, payment, communication, notification) on container startup. Схема `listings` добавлена в ходе создания `listing-service`

---

## Infrastructure Configuration

### Docker Compose Files

| File                                           | Purpose                                            | Services Count                                                       |
|---------------------------------------------------|----------------------------------------------------|----------------------------------------------------------------------|
| `docker-compose.yaml`                          | Full development stack (infra + app commented out) | 14 infrastructure services                                           |
| `docker-compose-infra.yaml`                    | Minimal infrastructure                             | consul, services-database, keycloak, keycloak-database, minio, redis |
| `docker-compose-infra-with-observability.yaml` | Full observability stack                         | Adds prometheus, loki, tempo, alloy, grafana, elasticsearch, kafka                 |

### Infrastructure Components

| Service           | Image                     | Version | Port(s)                    | Purpose                                      |
|-------------------|---------------------------|---------|----------------------------|----------------------------------------------|
| consul            | consul                    | 1.15.3  | 8500                       | Service Discovery / Registry                 |
| services-database | postgres                  | 15      | 5438                       | Main application database                    |
| keycloak-database | postgres                  | 15      | 5435                       | Keycloak identity database                   |
| keycloak          | quay.io/keycloak/keycloak | 21.1.1  | 8090                       | Identity & Access Management (OIDC/OAuth2)   |
| minio             | minio/minio                | latest  | 9000 (API), 9001 (Console) | S3-compatible object storage          |
| redis             | redis                    | 7       | 6379                       | Caching & rate limiting backend             |
| kafka             | apache/kafka              | 4.2.0   | 9092, 29092                | Event streaming / message broker             |
| elasticsearch     | elasticsearch          | 8.13.0  | 9200                       | Search & indexing                            |
| prometheus        | prom/prometheus             | v3.9.1  | 9091                       | Metrics collection                           |
| loki             | grafana/loki             | main    | 3100                       | Log aggregation                      |
| tempo             | grafana/tempo             | 2.4.1  | 3200                       | Distributed tracing                          |
| alloy             | grafana/alloy             | v1.12.2  | 9080, 4318                 | OpenTelemetry collector / telemetry pipeline |
| grafana           | grafana/grafana         | 12.4.0  | 3000                       | Metrics visualization & dashboards           |

### Keycloak Realm Configuration

- **Realm file**: `infrastructure/keycloak/realm-export.json`
- **Realm name:** autodev
- **Client**: `autodev-marketplace` (confidential, client-secret auth)
- **Roles**: ADMIN, SELLER, BUYER
- **Users**: admin (ADMIN), seller1 (SELLER), buyer1 (BUYER) — all with password "password"

### Monitoring Configuration

| Component  | Config File(s)                                                                                | Key Settings                                                         |
|------------|--------------------------------------------------------------------------------------------|-----------------------------------------------------------------|
| Prometheus | `infrastructure/prometheus/prometheus.yml`                                                    | Scrapes alloy at localhost:9080, consul SD                           |
| Loki       | `infrastructure/loki/config/config.yml`                                                  | Local filesystem storage, no auth                                    |
| Tempo       | `infrastructure/tempo/config/tempo.yaml`                                                 | Local backend, OTLP gRPC on 4317, OTLP HTTP on 4318                  |
| Alloy       | `infrastructure/alloy/config.alloy`                                                          | Receives OTLP logs/metrics/traces, forwards to Prometheus/Loki/Tempo |

### Docker Configuration

#### Dockerfiles

- **api-gateway**: `services/api-gateway/Dockerfile`
    - Multi-stage build with Gradle 8.14.5
    - Uses BellSoft Liberica JDK 17
    - Spring Boot Layered JAR approach
    - Exposes port 8080
- **platform-service**: `services/platform-service/Dockerfile`
- **Other services**: No Dockerfiles exist yet

#### Configuration Files

#### api-gateway

- **Primary**: `services/api-gateway/src/main/resources/application.yml`
- **Profile**: `docker` (для контейнеризованного развертывания)
- **Key Configs**: Redis, OAuth2 JWT, Gateway routes, Consul, Resilience4j

#### platform-service

- **Primary**: `services/platform-service/src/main/resources/application.yml`
- **Key Configs**: PostgreSQL datasource, JPA, Redis, Kafka, Consul, Resilience4j
- **Изменение (WIP)**: блок `keycloak.admin.*` (server-url, realm, client-id, client-secret) удалён из `application.yml` — конфигурация Keycloak Admin API переводится на окружение (`KEYCLOAK_HOST/PORT/CLIENT_SECRET` в `.env`). Класс `KeycloakProperties` (prefix `keycloak.admin`) и `KeycloakAdminClient` по-прежнему существуют и ждут привязки значений

---

## Continuous Integration

### GitHub Actions

Проект использует три workflow-файла (файл `gradle-ci.yaml` из документации больше не существует — заменён на `ci.yml`):

| Workflow | Файл | Назначение |
|----------|------|------------|
| **CI** | `.github/workflows/ci.yml` | Запускается на push/PR в ветку `develop`. Определяет изменённые сервисы (`dorny/paths-filter`) и по матрице выполняет: Checkstyle, SpotBugs, unit-тесты + Jacoco-отчёт и проверку покрытия, OWASP Dependency-Check (security-scan), интеграционные тесты (только push), агрегатор `CI Passed` для branch protection |
| **CodeQL** | `.github/workflows/codeql.yml` | Security-анализ (Java/Kotlin) на push/PR в `develop`, `main` + еженедельно по cron. Результаты загружаются в Security tab |
| **Release** | `.github/workflows/release.yml` | Публикация релиза по веткам `release/**` (парсит имя ветки для определения сервиса, версии и режима) |

> В `ci.yml` в списке фильтров `detect-changes` отсутствует `listing-service` — при активации сервиса его нужно добавить по аналогии с остальными.

## Environment Variables

| Variable                  | Purpose         | Default Value                        |
|---------------------------|-----------------|--------------------------------------|
| `PLATFORM_SERVICE_PORT`   | Service port    | 8081 (platform; ранее `SERVER_PORT`) |
| `PLATFORM_DB_HOST`        | DB host         | localhost                            |
| `PLATFORM_DB_PORT`        | DB port         | 5438                               |
| `PLATFORM_DB_NAME`        | DB name         | services                           |
| `PLATFORM_DB_USER`        | DB user         | postgres                           |
| `PLATFORM_DB_PASS`        | DB password     | postgres                           |
| `CATALOG_DB_HOST`         | Catalog DB host | localhost                          |
| `CATALOG_DB_PORT`         | Catalog DB port | 5438                              |
| `CATALOG_DB_USER`         | Catalog DB user | postgres                          |
| `CATALOG_DB_PASS`         | Catalog DB pass | postgres                          |
| `CATALOG_DB_NAME`         | Catalog DB name | services                          |
| `REDIS_HOST`              | Redis host      | localhost                          |
| `REDIS_PORT`              | Redis port      | 6379                              |
| `REDIS_PASSWORD`          | Redis password  | redis                             |
| `CONSUL_HOST`             | Consul host     | localhost                          |
| `CONSUL_PORT`             | Consul port     | 8500                              |
| `KEYCLOAK_HOST`           | Keycloak host   | localhost                          |
| `KEYCLOAK_PORT`           | Keycloak port   | 8090                              |
| `KEYCLOAK_CLIENT_SECRET`  | Keycloak client secret | (задаётся в `.env`)            |
| `KEYCLOAK_ISSUER_URI`     | Keycloak issuer | http://localhost:8090/realms/autodev  |
| `KAFKA_BOOTSTRAP_SERVERS` | Kafka servers   | localhost:9092                       |

> В `application.yml` platform-service ключ issuer-uri читается как `KEYCKLOAK_ISSUER_URI` (опечатка в имени переменной), а не `KEYCLOAK_ISSUER_URI` — при унификации конфигурации это стоит исправить.

---

## Architecture Patterns

1. **Multi-Database Architecture**: Each service may have its own database schema or instance
2. **Event-Driven Communication**: Kafka for inter-service messaging (outbox pattern)
3. **API Gateway Pattern**: Centralized routing, rate limiting, JWT validation
4. **Circuit Breaker**: Resilience4j for fault tolerance
5. **Observability Stack**: Prometheus + Grafana + Loki + Tempo (OpenTelemetry)
6. **Service Discovery**: Consul for dynamic service registration
7. **Read/Write Separation**: Catalog (Read-only search) vs Listings (Write-heavy CRUD) for independent scaling.

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
```

