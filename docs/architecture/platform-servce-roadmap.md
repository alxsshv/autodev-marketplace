Вот подробная Roadmap по разработке `platform-service`, составленная с учетом обновленной архитектуры (Direct JWT Propagation, Database per Service, Transactional Outbox).

Текущее состояние сервиса: инфраструктура (Security, Config, Liquibase, Exception Handling) готова на ~30%. Отсутствует бизнес-логика, слой данных и интеграция с Kafka.

---

# Roadmap: Platform Service (MVP)

## Этап 0: Подготовка и исправление базы данных (0.5 дня)

Перед написанием кода нужно привести существующую БД в соответствие с новой архитектурой.

1. **Исправить опечатку в Liquibase:**
   В текущей миграции `07-06-2026-create-table-user-profiles.sql` есть поле `loyality_balance`. Переименовать его в `loyalty_balance`.
2. **Создать таблицу Outbox:**
   Добавить миграцию `v1.0.0/XX-XX-2026-create-table-outbox.sql` в `platform_db`.
   ```sql
   CREATE TABLE platform.outbox_events (
       id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
       aggregate_type VARCHAR(255) NOT NULL,
       aggregate_id UUID NOT NULL,
       event_type VARCHAR(255) NOT NULL,
       payload JSONB NOT NULL,
       created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
       status VARCHAR(50) NOT NULL DEFAULT 'PENDING'
   );
   CREATE INDEX idx_outbox_status_created ON platform.outbox_events(status, created_at);
   ```
3. **Создать таблицу отзывов:**
   Добавить миграцию `v1.0.0/XX-XX-2026-create-table-reviews.sql`.
   ```sql
   CREATE TABLE platform.reviews (
       id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
       product_id UUID NOT NULL,
       user_id UUID NOT NULL,
       rating SMALLINT NOT NULL CHECK (rating BETWEEN 1 AND 5),
       text TEXT,
       seller_reply TEXT,
       created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
       updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
   );
   CREATE INDEX idx_reviews_product_id ON platform.reviews(product_id);
   CREATE INDEX idx_reviews_user_id ON platform.reviews(user_id);
   ```

---

## Этап 1: Доменная модель и слой данных (1 день)

Создание JPA сущностей и репозиториев. Обязательно использовать-schema в аннотациях, так как мы перешли на логические БД (хотя для `platform_db` схема по умолчанию `public`, лучше явно указывать для будущего).

1. **Сущность профиля (`UserProfileEntity`)**
    - Маппинг на `platform.user_profiles`.
    - Использовать `@Table(name = "user_profiles", schema = "platform")`.
2. **Сущность отзыва (`ReviewEntity`)**
    - Маппинг на `platform.reviews`.
3. **Сущность Outbox (`OutboxEntity`)**
    - Маппинг на `platform.outbox_events`.
    - Поле `payload` маппить как `@JdbcTypeCode(SqlTypes.JSON)`.
4. **Репозитории (Spring Data JPA)**
    - `UserProfileRepository`: метод `findByKeycloakUserId(UUID keycloakUserId)`.
    - `ReviewRepository`: методы `findByProductId(UUID productId)`, `findByUserId(UUID userId)`.
    - `OutboxRepository`: метод `findByStatusOrderByCreatedAtAsc(String status)`.

---

## Этап 2: Профили пользователей (1.5 дня)

Реализация CRUD для профилей с использованием **Direct JWT Propagation**.

1. **DTO и MapStruct Mapper:**
    - `UserProfileResponseDto` (id, email, username, storeName, avatarUrl).
    - `UpdateProfileRequestDto` (storeName, storeDescription, phone).
    - `UserProfileMapper` (MapStruct).
2. **Сервис `UserProfileService`:**
    - `getOrCreateProfile(UUID keycloakUserId, String email, String username)`: Метод проверяет, есть ли профиль в БД. Если нет — создает базовый. Это нужно, чтобы профиль создавался "на лету" при первом логине, без отдельного вызова регистрации.
    - `getCurrentUserProfile()`: Берет `keycloakUserId` из `SecurityUtils.getCurrentUserId()` (который достает `sub` из JWT), вызывает `findByKeycloakUserId`, маппит в DTO.
    - `updateCurrentUserProfile(UpdateProfileRequestDto dto)`: Обновление данных.
3. **Контроллер `UserProfileController`:**
    - `GET /api/v1/platform/users/me` -> `getCurrentUserProfile()`.
    - `PATCH /api/v1/platform/users/me` -> `updateCurrentUserProfile()`.

---

## Этап 3: Регистрация и интеграция с Keycloak (1.5 дня)

Синхронный вызов внешней системы (Keycloak Admin API) с сохранением локальных данных.

1. **Keycloak Admin Client:**
    - Создать конфигурационный класс `KeycloakAdminClientConfig`.
    - Настроить `WebClient` (или RestTemplate) для обращения к `http://keycloak:8080/admin/realms/autodev/users`.
    - Получать admin-токен через `client_credentials` grant type (настроить service account в Keycloak для platform-service).
2. **DTO регистрации:**
    - `RegisterRequestDto` (email, password, firstName, lastName).
3. **Сервис `RegistrationService`:**
    - Метод `registerUser(RegisterRequestDto dto)`:
        1. Вызов Keycloak Admin API (создание пользователя).
        2. Если Keycloak вернул 201 Created -> извлечь `userId` (Location header).
        3. Сохранить `UserProfileEntity` в `platform_db` (в рамках `@Transactional`).
4. **Контроллер `RegistrationController`:**
    - `POST /api/v1/platform/users/register` (доступен без авторизации `permitAll()` в `SecurityConfig`).

---

## Этап 4: Отзывы (Рейтинги) (1.5 дня)

CRUD отзывов. Здесь мы впервые применим **синхронный межсервисный вызов** (Service-to-Service) через OpenFeign, чтобы продемонстрировать работу с Circuit Breaker.

1. **Feign Client (в `platform-service`):**
    - Создать интерфейс `CatalogServiceClient`.
    - Метод `@GetMapping("/api/v1/catalog/products/{id}") ResponseEntity<?> checkProductExists(@PathVariable UUID id);`
    - *Настроить `JwtFeignInterceptor` (из стандартов коммуникации), чтобы передавать токен текущего пользователя в `catalog-service`.*
2. **DTO и Mapper:**
    - `CreateReviewRequestDto` (productId, rating, text).
    - `ReviewResponseDto`.
3. **Сервис `ReviewService`:**
    - `createReview(CreateReviewRequestDto dto)`:
        1. Вызвать `CatalogServiceClient.checkProductExists()`. Обернуть вызов в **Resilience4j CircuitBreaker**. Если `catalog-service` недоступен — выбросить `ServiceUnavailableException`.
        2. Сохранить `ReviewEntity`.
    - `getReviewsByProduct(UUID productId)`.
    - `addSellerReply(UUID reviewId, String replyText)` (с проверкой, что текущий пользователь — владелец товара. *Для MVP: упрощаем, проверяем просто по роли SELLER в JWT*).
4. **Контроллер `ReviewController`:**
    - `POST /api/v1/platform/reviews`
    - `GET /api/v1/platform/reviews/product/{productId}`
    - `POST /api/v1/platform/reviews/{id}/reply`

---

## Этап 5: Transactional Outbox Pattern (2 дня)

Самый важный этап для изучения распределенных систем. Гарантирует, что событие в Kafka точно будет отправлено, если транзакция в БД успела коммитнуться.

1. **События (Records):**
    - Создать пакеты `events` и `eventpayloads`.
    - Базовый record `DomainEvent` (eventId, eventType, timestamp, traceId, payload).
    - `UserRegisteredEventPayload` (userId, email).
    - `ReviewLeftEventPayload` (productId, userId, rating).
2. **Сервис `OutboxService`:**
    - Метод `saveEvent(String aggregateType, UUID aggregateId, String eventType, Object payload)`:
        - Сериализует payload в JSON (Jackson).
        - Сохраняет `OutboxEntity` в БД.
3. **Интеграция Outbox в бизнес-логику:**
    - В `RegistrationService.registerUser()`: после сохранения профиля вызвать `outboxService.saveEvent("User", userId, "UserRegistered", payload)`. **Всё это в одной `@Transactional`!**
    - В `ReviewService.createReview()`: аналогично отправить `ReviewLeft`.
4. **Outbox Poller (Паблишер):**
    - Создать `@Component` класс `OutboxEventPublisher`.
    - Метод с аннотацией `@Scheduled(fixedDelayString = "${outbox.polling.interval:500}")`.
    - Логика:
        1. Достать пачку (limit 50) записей со статусом `PENDING` из `OutboxRepository`.
        2. Отправить их в Kafka (`kafkaTemplate.send()`).
        3. Изменить статус на `SENT` в БД.
    - *Важно:* Использовать `@Transactional(propagation = Propagation.REQUIRES_NEW)` для метода обновления статуса, чтобы каждое событие подтверждалось отдельно.

---

## Этап 6: Тестирование (2 дня)

Использование Testcontainers для проверки распределенных сценариев.

1. **Unit-тесты:**
    - Тесты для `UserProfileService`, `ReviewService` (с моками `CatalogServiceClient` и `UserProfileRepository`).
    - Проверка логики Circuit Breaker (что происходит, если Feign выбрасывает исключение).
2. **Интеграционные тесты (Testcontainers):**
    - Поднять `PostgreSQLContainer` и `KafkaContainer`.
    - **Сценарий 1 (Регистрация + Outbox):** Вызвать `registerUser`. Проверить, что в БД появился профиль И появилась запись в `outbox_events`. Запустить `OutboxEventPublisher` вручную. Проверить, что сообщение появилось в Kafka (используя `@EmbeddedKafka` или Consumer от Testcontainers).
    - **Сценарий 2 (Отзыв + Feign fallback):** Запустить мок-сервер (WireMock) на порту catalog-service. Имитировать 500 ошибку. Проверить, что сервис выбрасывает ожидаемое исключение и отзыв НЕ сохраняется в БД.

---

## Итоговая статистика по Roadmap
- **Сроки:** ~8-9 рабочих дней на чистую разработку.
- **Изученные паттерны:**
    1. Direct JWT Propagation (извлечение данных напрямую из SecurityContext).
    2. Синхронные вызовы через OpenFeign + Resilience4j Circuit Breaker.
    3. Интеграция с внешним IAM (Keycloak Admin API).
    4. **Transactional Outbox Pattern** (ключевой навык из этой roadmap).
    5. Интеграционное тестирование распределенных транзакций.