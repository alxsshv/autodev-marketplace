# AutoDev Marketplace — System Architecture Overview
**Распределённая система продажи автозапчастей на стеке Java и Spring Boot**

*Версия документа: 2.0*
*Дата обновления: 2026-07-13*

---


## 1. Обзор системы

AutoDev Marketplace — это учебная платформа для продажи автозапчастей, построенная по принципам современных распределённых систем. Проект предназначен для глубокого изучения Java backend-разработчиками best practices разработки, деплоя и отладки микросервисных приложений на Spring Boot 3.x и Java 17+.

Система предоставляет ключевые бизнес-ценности: удобный поиск запчастей по VIN и артикулам, безопасную покупку товара, автоматизированные уведомления и надёжную интеграцию компонентов через асинхронную шину сообщений.

**Целевая аудитория:**
- Покупатели автозапчастей
- Продавцы (магазины, разборки)
- Администраторы системы

---


## 3. Архитектурное представление

### 3.0. Названия сервисов и директорий (MVP: 7 сервисов)

| Человекочитаемое название | Название сервиса (service name) | Директория |
|---------------------------|--------------------------------|-----------|
| API Gateway | api-gateway | services/api-gateway |
| Catalog Service | catalog-service | services/catalog-service |
| Order Service | order-service | services/order-service |
| Communication Service | communication-service | services/communication-service |
| Notification Service | notification-service | services/notification-service |
| Platform Service | platform-service | services/platform-service |

*Ключевые изменения для упрощения MVP:*

❌ Убран payment-service — логика оплаты (статусы оплаты, выбор способа) интегрирована в order-service. Выделение в отдельный сервис произойдет при интеграции реального платежного шлюза (ЮKassa, Сбербанк) в Post-MVP.
❌ Убран auth-service — используется прямая валидация JWT через Keycloak (Direct Keycloak Integration).
❌ Убран search-service — полнотекстовый поиск реализован через PostgreSQL FTS.


### 3.1. Контейнеры (C4 Level 2) - MVP: 7 сервисов

![Диаграмма С4L2](./diagram.svg)

### 3.2. Компоненты (C4 Level 3)


#### API Gateway:

- GatewayRoutingHandler — маршрутизация запросов к downstream-сервисам.
- RateLimitingFilter — распределенное ограничение запросов (Redis + Lua скрипты, fail-open).
- CORSFilter — управление кросс-доменными запросами.

Примечание: Gateway не парсит JWT, он выступает в роли слепого прокси (Blind Proxy) для токена аутентификации.

#### Catalog Service:

- ProductCatalogService — CRUD каталога товаров, управление объявлениями.
- CategoryService — иерархия категорий запчастей.
- VINLookupService — базовый подбор по VIN (без TecDoc).
- SearchService — полнотекстовый поиск (PostgreSQL FTS), фильтрация, сортировка. 
 
#### Order Service (включает базовый Payment):

- CartService — управление корзиной покупателя.
- OrderService — жизненный цикл заказа.
- DeliveryService — выбор способа доставки (без расчёта стоимости).
- PaymentProcessingService — управление статусами оплаты (наличные, базовая онлайн-оплата без интеграции).
- ReturnService — оформление возвратов.
- OrderOutboxService — формирование событий заказов для Kafka (Transactional Outbox).

#### Communication Service:

- ChatService — управление чатами (создание, список диалогов).
- WebSocketHandler — обработка real-time сообщений через WebSocket.

#### Notification Service:

- NotificationConsumer — прослушивание событий из Kafka.
- EmailSender — интеграция с SMTP для отправки email.
- SmsSender — заглушка/базовая интеграция для SMS.

#### Platform Service:

- UserProfileService — профили пользователей (синхронизировано с Keycloak).
- ReviewService — отзывы и рейтинги.
- RegistrationService — вызов Keycloak Admin API для создания аккаунта.
---


## 4. Ключевые архитектурные решения

### Стиль архитектуры

#### Микросервисная архитектура выбрана для изучения распределённых вычислений:

- Изоляция данных (Database per Service).
- Обработка сетевых сбоев, таймаутов и задержек (Resilience4j).
- Асинхронное взаимодействие и конечная согласованность (Eventual Consistency) через Kafka.
- Независимое развертывание (Docker) и масштабирование.

#### Межсервисная аутентификация (Direct JWT Propagation)*

Вместо антипаттерна "Gateway парсит токен и передает внутренние заголовки", применяется стандартный для Spring Security подход:

- API Gateway выполняет только маршрутизацию и Rate Limiting. Он пробрасывает оригинальный заголовок Authorization: Bearer <JWT> в downstream-сервисы без изменений.
- Каждый downstream-сервис конфигурируется как OAuth2 Resource Server.
- Сервисы самостоятельно валидируют подпись JWT, обращаясь к JWKS (JSON Web Key Set) эндпоинту Keycloak. Ключи кэшируются локально в памяти приложения.
- Роли (RBAC) извлекаются из валидированного JWT токена напрямую в SecurityContext каждого сервиса с помощью JwtAuthenticationConverter.

- Преимущества: Отсутствие дублирующей логики маппинга заголовков, защита от подмены заголовков внутри Docker-сети, полное соответствие стандартам Spring Security.

#### Коммуникация между сервисами

- Синхронная (REST): Используется только там, где бизнес-процессу нужен немедленный ответ (например, проверка наличия товара в Catalog Service при добавлении в корзину в Order Service). Вызовы защищены через Resilience4j Circuit Breaker.
- Асинхронная (Kafka): Используется для слабо связанных процессов (отправка уведомлений, обновление статистики, синхронизация данных).

#### Паттерны проектирования (Event-Driven)

- Transactional Outbox Pattern: Критически важный паттерн для распределенных систем. Сервис (например, Order Service) записывает событие в специальную таблицу outbox в рамках той же транзакции БД, что и бизнес-данные. Отдельный фоновый поток (или Debezium) читает outbox и публикует события в Kafka. Это гарантирует, что событие не потеряется при падении приложения после коммита БД.
- Event-Driven Architecture (Pub/Sub): Сервисы публикуют факты изменений состояния (доменные события: OrderCreated, UserRegistered), не зная, кто их потребит. Notification Service подписывается на нужные топики.

#### Инфраструктура высокой доступности и масштабирования
Horizontal Pod/Container Scaling: Stateless сервисы (Gateway, Catalog, Platform) масштабируются горизонтально.
Circuit Breaker & Retry (Resilience4j): Защита от каскадных сбоев при падении зависимых сервисов.
---


## 5. Основные функциональные модули

### API Gateway

Единая точка входа. Выполняет маршрутизацию запросов, ограничение частоты запросов (Rate Limiting через Redis) и управление CORS. Является "слепым" прокси для JWT токенов.

### Platform Service

Управление профилями пользователей (синхронизация с Keycloak при регистрации) и система отзывов/рейтингов. Является источником доменных событий UserRegistered, ReviewLeft.

### Catalog Service

Единственный источник правды (Single Source of Truth) для данных о товарах, категориях и наличии. Предоставляет полнотекстовый поиск без использования внешних поисковых движков (PostgreSQL FTS).

### Order Service

Управляет корзиной и жизненным циклом заказа (включая выбор способа доставки и фиксацию способа оплаты). Содержит самую сложную бизнес-логику распределенной транзакции: при создании заказа синхронно резервирует товар в Catalog Service (через REST + Circuit Breaker) и асинхронно публикует событие в Kafka (через Outbox).

### Communication Service

Обеспечивает real-time обмен сообщениями между покупателями и продавцами на базе WebSocket. Изучает специфику поддержания долгоживущих соединений в микросервисной среде.

### Notification Service

Типичный представитель "Consumer-only" микросервиса. Не предоставляет собственных REST API для бизнес-логики, только потребляет события из Kafka и вызовет внешние SMTP/SMS шлюзы.

## 6. Нефункциональные требования (NFR)


| Требование | Значение                              | Метрика |
|-----------|---------------------------------------|--------|
| **Производительность** | Время отклика < 500 мс                | p95 latency |
| **Доступность** | 99.5%                                 | Uptime |
| **Безопасность** | JWT (RS256), CORS, Rate Limiting      | OWASP Top 10|
| **Наблюдаемость** | Логи, метрики, трейсы по всей цепочке | Distributed Tracing |



---


## 7. Данные и хранение

В распределенной системе каждый сервис должен владеть своими данными. Для MVP используется один физический сервер PostgreSQL 15, но логически разделенный на 6 независимых баз данных. Это исключает взаимную блокировку (lock contention) на уровне СУБД и позволяет в будущем легко вынести базы на разные серверы.

|Логическая БД | Владелец   | Основные сущности                       |
|--------------|------------|-----------------------------------------|
|catalog_db |	Catalog Service | 	products, categories, product_images   |
|order_db |	Order Service | 	orders, order_items, carts, outbox     |
|platform_db |	Platform Service | 	user_profiles, reviews, outbox         |
|communication_db |	Communication Service | 	chats, messages     |                   
|notification_db |	Notification Service | 	notification_history (idempotency key) | 
|keycloak_db |	Keycloak | 	Системные таблицы IAM                  |

## 8. Технологический стек

### Бэкенд
Java: 17 (LTS)
Spring Boot: 3.4.5
Spring Cloud: 2024.0.0 (Gateway, Consul)
Spring Security OAuth2 Resource Server: Прямая валидация JWT
Spring Data JPA: 3.4.5
Resilience4j: 2.1.0 (Circuit Breaker, Retry)
Liquibase: 4.27.0 (Миграции БД)
MapStruct: 1.5.2, Lombok: 1.18.34

### Базы данных и кэширование
PostgreSQL: 15 (Логически изолированные БД на сервис)
Redis: 7 (Кэш, Rate Limiting)
Apache Kafka: 3.7.0 (Асинхронная шина сообщений)
MinIO: (Хранилище фото товаров)

### Инфраструктура и Observability (Grafana LGTM Stack)
Docker & Docker Compose: Оркестрация
Consul: 1.15.3 (Service Discovery)
Keycloak: 21.1.1 (IAM)
Prometheus: Сбор метрик
Grafana: Визуализация дашбордов
Loki: Агрегация логов (с поддержкой trace_id)
Tempo: Распределенная трассировка
Alloy: Агент для сбора телеметрии (OTLP)

### Тестирование
JUnit 5, Mockito: Unit-тесты
Testcontainers: Интеграционные тесты с поднятием реальных БД, Redis, Kafka
WireMock: Мокирование внешних REST API (например, Keycloak Admin API)

---

## 9. Безопасность

### 9.1 Модель аутентификации (Zero-Trust к Gateway)
Сервисы не доверяют API Gateway в вопросах идентификации пользователя.

- Клиент получает JWT токен напрямую из Keycloak (через фронтенд или прямым вызовом).
- Запрос с Authorization: Bearer <token> проходит через API Gateway.
- Downstream-сервис проверяет криптографическую подпись токена с помощью публичных ключей Keycloak (JWK Set endpoint). Ключи кэшируются в приложении с TTL.
- Извлечение sub (User ID) и realm_access.roles происходит из JwtAuthenticationToken в Spring Security.

###  9.2 RBAC (Role-Based Access Control)

- Роли (BUYER, SELLER, ADMIN) хранятся внутри JWT токена в Keycloak.
- Авторизация на уровне эндпоинтов выполняется стандартными аннотациями Spring Security: @PreAuthorize("hasRole('SELLER')").
- Межсервисные вызовы (если требуются права сервиса) используют Service Accounts Keycloak.

### 9.3 Безопасность данных

- Пароли хешируются BCrypt внутри Keycloak.
- Внешний трафик (в проде) защищается TLS.
- Внутренняя сеть Docker изолирована.

### 10. Стратегия развёртывания (MVP)
Для локальной разработки и учебных целей используется Docker Compose, который поднимает весь стек (6 сервисов + инфраструктура) одной командой.

- Каждый сервис собирается в свой Docker image (multi-stage build).
- Используются healthchecks для контроля готовности сервисов (Readiness/Liveness probes).
- Базы данных инициализируются автоматически при первом запуске (init-scripts для создания логических БД).