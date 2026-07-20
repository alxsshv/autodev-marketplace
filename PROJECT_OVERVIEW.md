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
- Эскроу и интеграции с банками
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

### Полный состав (Post-MVP)

**Дополнительные сервисы:**
- **auth-service** — централизованная аутентификация (для Post-MVP)
- **search-service** — Elasticsearch для продвинутого поиска
- **admin-service** — админ-консоль с модерацией
- **analytics-service** — аналитика и отчёты
- **marketing-service** — акции, купоны, маркетинг
- **loyalty-service** — система лояльности и скидок
- **integrations-service** — интеграции с внешними системами

---

## Технологический стек

### Бэкенд
- **Java 17** (LTS версия)
- **Spring Boot 3.4.5**
- **Spring Cloud 2024.0.0** (Gateway, Consul, Security)
- **Spring Data JPA**, **Liquibase**
- **MapStruct**, **Lombok**
- **JUnit 5**, **Mockito**, **Testcontainers**, **WireMock**

### Базы данных и кэширование
- **PostgreSQL 15** — основная реляционная БД
- **Redis 7** — кэширование, сессии
- **Apache Kafka 7.3.2** — асинхронная коммуникация
- **MinIO 2023.05.14** — объектное хранилище

### Инфраструктура и мониторинг
- **Docker**, **Docker Compose**
- **Consul 1.15.3** — Service Discovery
- **Keycloak 21.1.1** — IAM
- **Prometheus**, **Grafana**, **Loki**, **Tempo**, **Alloy** — Observability

### Сборка
- **Gradle 8+ c Kotlin DSL**

---

## Текущее состояние реализации

### API Gateway — ПОЛНОСТЬЮ РЕАЛИЗОВАН

**6 основных Java-классов:**
- `GatewayApplication.java` — точка входа Spring Cloud Gateway
- `SecurityConfig.java` — OAuth2 Resource Server с Keycloak, CORS, rate limiting
- `RateLimitingProperties.java` — конфигурационные properties для rate limiting
- `RateLimitingFilter.java` — Redis + Lua для distributed rate limiting (fail-open, скользящее окно)
- `HeadersEnrichmentFilter.java` — извлечение и проброс JWT claims в downstream сервисы
- `KeycloakReactiveJwtAuthenticationConverter.java` — конвертер JWT токенов Keycloak для reactive stack

**Конфигурация:**
- `application.yml` — маршруты к 6 downstream сервисам, Redis, Consul, Keycloak, Resilience4j (circuit breaker, retry, timeout), OTEL трассировка
- `logback-spring.xml` — логирование
- `Dockerfile` — multi-stage сборка с Spring Boot layertools

**Тестирование (6 integration tests):**
- `AuthenticationIntegrationTest.java` — проверка OAuth2 аутентификации
- `CorsIntegrationTest.java` — проверка CORS-заголовков
- `RateLimitingIntegrationTest.java` — проверка rate limiting
- `RateLimitingFailOpenIntegrationTest.java` — проверка fail-open режима при отказе Redis
- `ApiGatewayRoutingTest.java` — проверка маршрутизации к downstream сервисам
- `AbstractIntegrationTest.java` — базовый класс с Testcontainers (Redis, WireMock)

**Итого:** ~3000 строк кода, полное покрытие основных сценариев интеграционными тестами.

---

### Platform Service — ЧАСТИЧНО РЕАЛИЗОВАН

**7 основных Java-классов:**

*Инфраструктура и безопасность:*
- `PlatformServiceApplication.java` — точка входа Spring Boot приложения
- `SecurityConfig.java` — Spring Security с OAuth2 Resource Server, method security
- `KeycloakJwtAuthenticationConverter.java` — извлечение ролей из Keycloak JWT (realm_access claim)
- `SecurityUtils.java` — утилита для получения текущего user ID, email, ролей из SecurityContext

*Обработка ошибок:*
- `GlobalExceptionHandler.java` — `@RestControllerAdvice` для validation errors, JSON parse errors, access denied, общих исключений
- `ErrorResponse.java` — record для стандартизированного REST error response
- `FieldViolation.java` — record для field-level violation errors

**Миграции БД (Liquibase, 6 файлов):**
- `master.yaml` — корневой changelog
- `07-06-2026-create-table-user-profiles.sql` — создание таблицы `platform_service.user_profiles` (id, user_id, keycloak_user_id, store_name, store_description, store_logo_url, verification_status, loyality_balance, created_at, updated_at)
- `07-06-2026-add-avatar-to-profile.sql` — добавление колонки `avatar_url`
- 4 индексных файла (store_name, user_id, verification_status)

**Конфигурация:**
- `application.yml` — PostgreSQL, JPA (ddl-auto: validate), Liquibase, Redis, Kafka (producer/consumer), Keycloak OAuth2, Consul discovery, Resilience4j circuit breaker

**build.gradle.kts** — полный: Spring Web, Data JPA, Data Redis, Security, OAuth2 Resource Server, Validation, Kafka, Liquibase, PostgreSQL, MapStruct, Lombok, Resilience4j, WireMock, TestContainers

**Что отсутствует (не реализовано):**
- ❌ JPA `@Entity` классы (нет маппинга на существующие таблицы)
- ❌ Spring Data `@Repository` интерфейсы
- ❌ `@Service` классы бизнес-логики
- ❌ `@RestController` (кроме `@RestControllerAdvice`)
- ❌ DTO/Record для API запросов/ответов (кроме ErrorResponse, FieldViolation)
- ❌ MapStruct mapper'ы

---

### Прочие сервисы — ПУСТЫЕ ПРОЕКТЫ (placeholder)

| Сервис | build.gradle.kts | src/ | application.yml | Java-код |
|--------|:-:|:-:|:-:|:-:|
| **catalog-service** | Пустой (0 байт) | ❌ | ❌ | ❌ |
| **order-service** | Пустой (0 байт) | ❌ | ❌ | ❌ |
| **payment-service** | Пустой (0 байт) | ❌ | ❌ | ❌ |
| **communication-service** | Пустой (0 байт) | ❌ | ❌ | ❌ |
| **notification-service** | Пустой (0 байт) | ❌ | ❌ | ❌ |

Все 5 сервисов содержат только пустой `build.gradle.kts` — ни одного Java-файла, ни одного конфигурационного файла.

---

### Инфраструктура

**Docker Compose:**
- `docker-compose.yaml` — полный стек (~20+ контейнеров): Consul, PostgreSQL 15 (multi-schema), Keycloak 21.1.1 + своя БД, MinIO, Redis 7, Kafka 4.2.0, Prometheus, Loki, Tempo, Alloy, Grafana 12.4, Elasticsearch 8.13, exporter'ы
- `docker-compose-infrastructure.yaml` — облегчённая версия (без api-gateway, Elasticsearch, Kafka)
- API Gateway в `docker-compose.yaml` закомментирован

**База данных:**
- `infrastructure/service-db/init/init-schemas.sql` — создание схем: `catalog`, `order`, `payment`, `platform`, `communication`, `notification`
- Единый PostgreSQL инстанс для всех сервисов (multi-schema)

**CI/CD:**
- `.gitlab-ci.yml` — пайплайн для GitLab CI
- `.github/workflows/check-commit-size.yml` — GitHub Actions для проверки размера коммитов
- `.env` — переменные окружения

**Сборка:**
- `build.gradle.kts` (корневой) — общая конфигурация
- `settings.gradle.kts` — включение всех 7 модулей
- `gradle/libs.versions.toml` — version catalog (Spring Boot 3.4.5, Spring Cloud 2024.0.0, и все зависимости)

---

## Архитектурные паттерны

- **API Gateway** — единая точка входа
- **Service Discovery** — Consul
- **Circuit Breaker** — Resilience4j
- **Saga Pattern** — для распределённых транзакций
- **Event Sourcing** — через Kafka
- **Direct Keycloak Integration** — аутентификация без auth-service
- **PostgreSQL FTS** — полнотекстовый поиск без search-service

---

## Инфраструктура и мониторинг

- **docker-compose.yaml** с 20+ контейнерами
- **Логирование** — Loki
- **Метрики** — Prometheus + Grafana
- **Трассировка** — Tempo + Alloy
- **Alerting** и аудит безопасности

---

## План развития

### MVP (релиз 1.0)
- 7 микросервисов
- Базовая функциональность
- Direct Keycloak Integration
- PostgreSQL FTS вместо Elasticsearch

### Post-MVP (релиз 2.0+)
- Дополнительные сервисы (auth, search, analytics, marketing)
- Эскроу и интеграции с банками
- Продвинутая аналитика и маркетинг
- Лояльность и скидки

---

**Версия документа:** 1.0  
**Дата создания:** 2026-07-13  
**Статус:** MVP в разработке
