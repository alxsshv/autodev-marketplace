# Фаза 0 — Каркас сервиса

## 0.1. Зависимости (build.gradle.kts)

spring-boot-starter-web, spring-boot-starter-data-jpa, spring-boot-starter-security
spring-boot-starter-oauth2-resource-server (JWT валидация через Keycloak)
spring-cloud-starter-consul-discovery
spring-kafka
data-redis (кэш профилей)
mapstruct, lombok
postgresql, liquibase
resilience4j-spring-boot3 (circuit breaker для вызовов Keycloak)
Тестовые: testcontainers, wiremock, mockito

## 0.2. Конфигурация (application.yml)

Подключение к PostgreSQL (через переменные окружения)
Consul registration (spring.cloud.consul)
Keycloak Resource Server (spring.security.oauth2.resourceserver.jwt)
Kafka producer/ consumer настройки
Redis для кэширования
Resilience4j: circuit breaker для Keycloak client

## 0.3. Security контекст

SecurityConfig — настройка JwtAuthenticationConverter с извлечением ролей из realm roles
Утилитный класс SecurityUtils — получение userId, email, roles из Authentication
Проверка, что токен валидирован (Spring Security делает это автоматически через Resource Server)

## 0.4. Инфраструктурный слой

@ControllerAdvice с обработчиками исключений (MethodArgumentNotValidException, AccessDeniedException, бизнес-исключения)
Стандартная структура ответов: ErrorResponse, FieldError
@MappedSuperclass базовая сущность (если её нет в миграциях — id, createdAt, updatedAt)


# Фаза 1 — UserProfileService

## 1.1. Сущность и репозиторий

JPA-сущность UserProfile — свериться с миграцией user-profiles, накатить схему
UserProfileRepository extends JpaRepository + кастомные методы:
findByKeycloakUserId(String keycloakUserId)
existsByKeycloakUserId(String keycloakUserId)

## 1.2. DTO и маппинг

UserProfileResponse — публичное представление профиля (без чувствительных данных)
UpdateProfileRequest — поля для редактирования (имя, телефон, адрес)
UserProfileMapper (MapStruct) — маппинг сущность ↔ DTO

## 1.3. Keycloak REST Client

KeycloakClient — REST-клиент для вызова Keycloak Admin API:
createUser() — создание пользователя в Keycloak (email, пароль, включение в роль BUYER/SELLER)
getUserById() — получение данных из Keycloak
updateUser() — обновление данных в Keycloak
Настройка RestClient или WebClient с @Bean
Circuit breaker (Resilience4j) на каждый вызов

## 1.4. Регистрация пользователя

RegisterRequest DTO (email, password, role — BUYER или SELLER)
Логика в UserProfileServiceImpl.register():
Валидация входных данных (email уникален, пароль соответствует политике)
keycloakClient.createUser() → получение keycloakUserId
Сохранение UserProfile в PostgreSQL с keycloakUserId
Отправка UserRegisteredEvent в Kafka (тopic: user-events)
Возврат UserProfileResponse
Обработка ошибок: если Keycloak упал → откат (не создаём профиль)

## 1.5. Получение профиля

GET /api/v1/profiles/me — профиль текущего пользователя (userId из JWT → findByKeycloakUserId)
GET /api/v1/profiles/{id} — публичный профиль по ID (только публичные поля)
Кэширование через @Cacheable (Redis): публичные профили кэшируются, при обновлении — инвалидация

## 1.6. Редактирование профиля

PATCH /api/v1/profiles/me — обновление полей профиля
Валидация: нельзя изменить keycloakUserId и email (email меняется через Keycloak отдельно)
Инвалидация кэша при обновлении

## 1.7. Контроллер

UserProfileController с эндпоинтами из 1.5 и 1.6
@PreAuthorize("hasRole('BUYER') or hasRole('SELLER')") на все методы
Документация через OpenAPI annotations

# Фаза 2 — ReviewService

## 2.1. Сущности и репозитории

Review — отзыв (id, authorId, sellerId, productId, rating, text, createdAt)
ReviewResponse — ответ продавца (id, reviewId, sellerId, text, createdAt)
Свериться с миграцией reviews
ReviewRepository:
findByProductId(UUID productId, Pageable pageable)
findBySellerId(UUID sellerId, Pageable pageable)
existsByAuthorIdAndProductId(UUID authorId, UUID productId) — защита от дубликатов
calculateAverageRatingBySellerId(UUID sellerId) — агрегация (или через отдельный запрос)
ReviewResponseRepository:
findByReviewId(UUID reviewId)

## 2.2. DTO и маппинг

CreateReviewRequest (rating 1–5, text — опционально)
CreateReviewResponseRequest (text)
ReviewResponse DTO — отзыв + вложенный ответ продавца (если есть)
SellerRatingResponse — агрегированный рейтинг (среднее, количество отзывов)
ReviewMapper (MapStruct)

## 2.3. Создание отзыва

POST /api/v1/reviews — только для BUYER
Логика:
Проверка: пользователь ещё не оставлял отзыв на этот товар
Валидация rating (1–5), text (не пустой или ограничение длины)
Сохранение отзыва
Пересчёт рейтинга продавца (или отложенный через события)
Отправка ReviewCreatedEvent в Kafka

## 2.4. Ответ продавца на отзыв

POST /api/v1/reviews/{reviewId}/responses — только для SELLER
Проверка: отзыв относится к товару этого продавца
Проверка: ответ ещё не был дан
Сохранение ReviewResponse

## 2.5. Получение отзывов

GET /api/v1/reviews/product/{productId} — отзывы на товар (пагинация)
GET /api/v1/reviews/seller/{sellerId} — отзывы о продавце (пагинация)
Каждый отзыв включает: автор (имя из профиля), текст, рейтинг, ответ продавца

## 2.6. Рейтинг продавца

GET /api/v1/profiles/{sellerId}/rating — средний рейтинг + количество отзывов
SQL-агрегация через @Query или отдельный метод в репозитории
Кэширование в Redis с инвалидацией при новом отзыве

## 2.7. Контроллер

ReviewController с эндпоинтами из 2.3–2.6
Разделение @PreAuthorize по ролям (BUYER для отзыва, SELLER для ответа)

# Фаза 3 — Kafka события

## 3.1. Модели событий

UserRegisteredEvent (keycloakUserId, email, role, timestamp)
ReviewCreatedEvent (reviewId, sellerId, productId, authorId, rating, timestamp)
Пакет events с отдельными record-классами

## 3.2. Producer

UserEventProducer — отправка в topic user-events
ReviewEventProducer — отправка в topic review-events
Конфигурация: ProducerConfig, ключи — UUID, сериализация — JSON

## 3.3. Consumer (заглушки)

Подписка на события из других сервисов (пока пустые, но структура готова)
Например: при удалении пользователя из Keycloak — архивация профиля

# Фаза 4 — Тестирование

## 4.1. Unit-тесты

UserProfileServiceImplTest — моки на KeycloakClient, UserProfileRepository, UserEventProducer
Сценарии: успешная регистрация, дубликат email, падение Keycloak (circuit breaker), редактирование
ReviewServiceImplTest — моки на репозитории, проверка бизнес-правил
Сценарии: создание отзыва, дубликат, ответ продавца, несуществующий отзыв

## 4.2. Интеграционные тесты

UserProfileControllerIntegrationTest — Testcontainers (PostgreSQL, Redis, Kafka), WireMock для Keycloak
Полная цепочка: POST /register → проверка в БД → событие в Kafka
ReviewControllerIntegrationTest — аналогично
Тест RBAC: запрос без токена → 401, с неправильной ролью → 403
Порядок выполнения

---

Фаза 0 (каркас)
0.1 → 0.2 → 0.3 → 0.4
↓
Фаза 1 (профили)
1.1 → 1.2 → 1.3 → 1.4 → 1.5 → 1.6 → 1.7
↓
Фаза 2 (отзывы)                    ↓
2.1 → 2.2 → 2.3 → 2.4 → 2.5 → 2.6 → 2.7
↓
Фаза 3 (Kafka)  ←──── можно параллельно с 2.3–2.7
3.1 → 3.2 → 3.3
↓
Фаза 4 (тесты)  ←──── можно писать параллельно с фазами 1–2
4.1 → 4.2
---

Итого: 20 задач, из которых критический путь — 14 задач (Фазы 0→1→2 без параллельных веток). Фаза 3 и 4 частично перекрываются с фазой 2.

