# AutoDev Marketplace — Сервисы для MVP и Post-MVP

**Версия документа:** 2.0
**Дата создания:** 2026-06-21
**Дата обновления:** 2026-07-13
**Статус:** Документ архитектурного анализа

---

## Предисловие

Этот документ содержит список микросервисов, необходимых для реализации MVP-функциональности (32 функции).

Архитектура пересмотрена с целью сохранения учебной ценности распределённых систем (изоляция данных, асинхронная коммуникация, сетевые сбои), но с устранением избыточности (over-engineering), которая тормозит разработку на ранних этапах.

*Ключевые изменения (v2.0):*

- Снижено количество сервисов с 7 до 6 (убран payment-service, логика перенесена в order-service).
- Утвержден паттерн Database per Service (отдельные логические БД вместо схем в одной БД).
- Исправлен паттерн аутентификации: Direct JWT Propagation (Gateway не парсит токены, валидация происходит в downstream-сервисах).
- Внедрен паттерн Transactional Outbox для надежной публикации событий в Kafka.

---

## MVP-функции и их реализация

### Категория 1: Поиск и каталог товаров (7 функций)

| Функция | Текущий сервис | БД | Примечание |
|---------|---------------|----|------------|
| Поиск по VIN-коду | catalog-service | ✅ catalog_db | VINLookupService |
| Поиск по артикулу | catalog-service | ✅ catalog_db | ProductCatalogService |
| Поиск по названию | catalog-service | ✅ catalog_db | PostgreSQL FTS (без Elasticsearch) |
| Поиск по марке/модели | catalog-service | ✅ catalog_db | CategoryService |
| Поиск по категории | catalog-service | ✅ catalog_db | CategoryService |
| Фильтрация | catalog-service | ✅ catalog_db | PostgreSQL FTS |
| Сортировка | catalog-service | ✅ catalog_db | PostgreSQL |


---

### Категория 2: Профили (3 функции)

| Функция | Текущий сервис       | БД            | Примечание |
|---------|----------------------|---------------|------------|
| Регистрация | platform-service     | ✅ platform_db | Вызов Keycloak Admin REST API + сохранение профиля |
| Аутентификация | api-gateway (прокси) | -             | Проброс JWT, валидация в downstream через JWK |
| Управление профилем | platform-service     | ✅ platform_db           | UserProfileService |



---

### Категория 3: Управление объявлениями (4 функции)

| Функция | Текущий сервис | БД| Примечание |
|---------|---------------|--------------|------------|
| Создание объявления | catalog-service | ✅ catalog_db | ProductCatalogService |
| Редактирование | catalog-service | ✅ catalog_db | ProductCatalogService |
| Архивация | catalog-service | ✅ catalog_db | ProductCatalogService |
| Продление | catalog-service | ✅ catalog_db | ProductCatalogService |

---

### Категория 4: Взаимодействие (1 функция)

| Функция | Текущий сервис | БД  | Примечание |
|---------|---------------|----|------------|
| Внутренний чат | communication-service | ✅ communication_db | WebSocket, базовый функционал |




---

### Категория 5: Оформление заказа (7 функций)

Примечание: payment-service объединен с order-service, так как в MVP нет реальной интеграции с платежными шлюзами.

| Функция | Текущий сервис | БД    | Примечание |
|---------|---------------|------|------------|
| Добавление в корзину | order-service | order_db | CartService |
| Оформление заказа | order-service | order_db | OrderService + Outbox (публикация OrderCreated) |
| Оплата картой | payment-service | order_db |  Базовая смена статуса (заглушка без банка) |
| Оплата при получении | order-service | order_db | Фиксация способа оплаты |
| Выбор доставки | order-service | order_db | DeliveryService (без расчёта стоимости) |
| Идемпотентность | order-service | order_db | Паттерн Idempotent Consumer / уникальные ключи |

---

### Категория 6: Управление заказами (4 функции)

| Функция | Текущий сервис | БД        | Примечание |
|---------|---------------|-----------|------------|
| Просмотр заказов | order-service | order_db  | OrderService |
| Отслеживание статуса | order-service | order_db  | OrderService |
| Отмена заказа | order-service | order_db  | OrderService + Outbox (OrderCancelled) |
| Возврат товара | order-service | order_db     | ReturnService |


---

### Категория 7: Рейтинги и отзывы (4 функции)

| Функция | Текущий сервис | БД           | Примечание |
|---------|---------------|--------------|------------|
| Оценка товара | platform-service | platform_db  | ReviewService |
| Текстовый отзыв | platform-service | platform_db         | ReviewService |
| Ответ продавца | platform-service | platform_db        | ReviewService |
| Рейтинг продавца | platform-service | platform_db         | Агрегация через Materialized View или запрос |

---

### Категория 8: Уведомления (2 функции)

| Функция | Текущий сервис | БД   | Примечание |
|---------|---------------|------|------------|
| Email уведомления | notification_db | ✅ Да | Kafka Consumer (UserRegistered, OrderCreated) |
| SMS уведомления | notification_db | ✅ Да | Kafka Consumer |



---

## Итоговый список сервисов для MVP

### Минимум микросервисов (7)

| Сервис | Описание | База данных                                                                             | Роль в Kafka                                       |        
|--------|----------|-----------------------------------------------------------------------------------------|----------------------------------------------------|
| **api-gateway** | 	Маршрутизация, Rate Limiting, CORS. Не трогает JWT.  | Нет              | Нет                                                |
| **catalog-service** | 	Каталог, поиск (FTS), объявления, категории      | catalog_db         | Consumer (слушает обновления профилей, если нужно) |
| **order-service** | Корзина, заказы, доставка, статусы оплаты, возвраты | order_db         | Producer (Outbox: OrderCreated, OrderCancelled)    |
| **communication-service** | Чат (WebSocket) | communication_db          | Нет                                                |
| **notification-service** | Отправка Email/SMS  | notification_db |      Consumer (слушает топики заказов и пользователей)    |
| **platform-service** | Профили (синхронизация с Keycloak), отзывы       | platform_db | Producer (Outbox: UserRegistered, ReviewLeft)|

❌ payment-service — статус оплаты теперь просто поле в orders таблице. Интеграция с ЮKassa/Сбером потребует выделения сервиса в Post-MVP.
❌ auth-service — использование стандарта Spring Security OAuth2 Resource Server напрямую в сервисах.
❌ search-service — PostgreSQL FTS.


---

## Аутентификация (Direct JWT Propagation)

В рамках MVP мы отказались от подхода, при котором API Gateway извлекает claims из JWT и передает их в кастомных заголовках (типа X-User-Id). Это антипаттерн, нарушающий Zero-Trust архитектуру и создающий дублирование кода.

Принятая цепочка (Blind Proxy + Local Validation):

![auth](./docs/architecture/images/auth.svg)
---


## Конфигурация downstream-сервиса (одинакова для всех, кроме gateway):

```
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: ${KEYCLOAK_ISSUER_URI:http://keycloak:8080/realms/autodev}
          # Spring автоматически заберет публичные ключи по JWKS эндпоинту
```

## Регистрация пользователя (Синхронный вызов Keycloak):

- POST /api/v1/platform/users/register -> platform-service
- platform-service синхронно вызывает Keycloak Admin REST API (создает пользователя).
- platform-service сохраняет профиль в platform_db в одной локальной транзакции.
- Через Transactional Outbox асинхронно публикует событие UserRegistered в Kafka.
- notification-service consumes событие и шлет Welcome Email.


## Паттерн интеграции: Transactional Outbox

Поскольку мы изучаем распределенные системы, критически важно не использовать "наивную" интеграцию, когда код пишет в БД, а затем делает kafkaTemplate.send(). При падении приложения между этими шагами данные будут потеряны.

**Реализация в MVP:**

- В order_db и platform_db создается таблица outbox_events.
- При сохранении бизнес-данных (например, нового заказа), в ту же транзакцию @Transactional в таблицу outbox_events записывается Kafka-событие.
- Отдельный @Scheduled таск читает outbox_events, публикует их в Kafka и помечает как отправленные.

## Рекомендация по развитию
Использовать 6 микросервисов с Database-per-Service и Direct JWT (рекомендуется для MVP)
Этот подход сохраняет 100% учебной ценности:

Вы изучаете сетевые вызовы (REST/OpenFeign) и их сбои (Circuit Breaker).
Вы изучаете (Eventual Consistency) через Kafka и Outbox.
Вы изучаете изоляцию данных (невозможно сделать JOIN между заказами и каталогом).
Вы изучаете правильную работу с JWT в распределенной среде.
Вы не тратите время на поддержку 7-го сервиса (payment), который в MVP просто меняет статус в БД.