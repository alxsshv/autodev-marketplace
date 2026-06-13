# AutoDev Marketplace API Specifications (MVP: 8 Сервисов)

**Версия документа:** 2.0  
**Дата создания:** 2026-06-04  
**Дата обновления:** 2026-06-04  

---

## Обзор

В этом каталоге содержатся OpenAPI 3.0 спецификации для публичных API AutoDev Marketplace **для MVP архитектуры из 8 сервисов**.

Для исторических спецификаций устаревших сервисов см. `docs/architecture/api-specification/legacy/`.

---

## Структура API спецификаций (MVP: 8 сервисов)

```
docs/architecture/api-specification/
├── api-gateway.yaml              # API Gateway - routing, rate limiting, JWT validation
├── auth-service.yaml             # Auth Service - authentication, authorization, JWT tokens
├── catalog-service.yaml          # Catalog Service - products, categories, VIN lookup, pricing, inventory
├── order-service.yaml            # Order Service - cart, orders, delivery, returns
├── search-service.yaml           # Search Service - full-text search, autocomplete, recommendations
├── payment-service.yaml          # Payment Service - payment processing, escrow, refunds
├── communication-service.yaml    # Communication Service - notifications, messaging, video calls
├── platform-service.yaml         # Platform Service - users, moderation, reviews, analytics, admin
└── README.md                     # This file (MVP version)
```

---

## Список MVP сервисов и их функции

| Сервис | Порт (Docker) | Порт (Local) | Описание |
|--------|---------------|--------------|----------|
| **api-gateway** | 8080 | 8081 | Единая точка входа, маршрутизация, rate limiting, JWT validation |
| **auth-service** | 8082 | 8082 | Аутентификация, авторизация, JWT токены, Keycloak |
| **catalog-service** | 8084 | 8084 | Каталог товаров, категории, VIN lookup, цены, наличие |
| **order-service** | 8085 | 8085 | Корзина, заказы, доставка, возвраты |
| **search-service** | 8086 | 8086 | Полнотекстовый поиск, автодополнение, рекомендации |
| **payment-service** | 8087 | 8087 | Обработка платежей, эскроу, возвраты |
| **communication-service** | 8088 | 8088 | Уведомления (email/SMS/push), чат, видеозвонки |
| **platform-service** | 8089 | 8089 | Пользователи, модерация, отзывы, аналитика, админка |

---

## Консолидация сервисов (25+ → 8 для MVP)

| Исходные сервисы | Консолидированный сервис |
|----------------|-------------------------|
| catalog-service | catalog-service (Products + Categories) |
| pricing-service | catalog-service (Prices) |
| inventory-service | catalog-service (Availability) |
| order-service | order-service (Orders + Cart) |
| logistics-service | order-service (Delivery) |
| returns-service | order-service (Returns) |
| notification-service | communication-service (Notifications) |
| messaging-service | communication-service (Messaging) |
| search-service | search-service |
| payment-service | payment-service |
| user-service | platform-service (Users + Profiles) |
| seller-dashboard-service | platform-service (Seller Dashboard) |
| moderation-service | platform-service (Moderation) |
| review-service | platform-service (Reviews) |
| analytics-service | platform-service (Analytics) |
| admin-service | platform-service (Admin) |
| marketing-service | platform-service (Marketing) |

---

## Использование спецификаций

### Генерация клиентского кода

```bash
# Генерация Java клиента для auth-service
openapi-generator-cli generate \
  -i docs/architecture/api-specification/auth-service.yaml \
  -g java \
  -o generated/client-auth-service \
  --additional-properties=library=resttemplate

# Генерация TypeScript клиента
openapi-generator-cli generate \
  -i docs/architecture/api-specification/platform-service.yaml \
  -g typescript-fetch \
  -o generated/client-platform-service
```

### Генерация документации Swagger UI

```bash
# Запуск Swagger UI с локальными спецификациями
docker run -p 8080:8080 \
  -e SWAGGER_JSON=/api-specification/auth-service.yaml \
  -v $(pwd)/docs/architecture/api-specification:/api-specification \
  swaggerapi/swagger-ui
```

### Валидация спецификаций

```bash
# Валидация спецификации с помощью openapi-generator
openapi-generator-cli validate \
  -i docs/architecture/api-specification/auth-service.yaml
```

---

## Соглашения по API

### Base URL

- **Production:** `https://api.autodev.marketplace`
- **Docker:** `http://<service-name>:8080`
- **Local:** `http://localhost:<port>`

### Versioning

- URL versioning: `/api/v1/...`
- Header versioning (альтернатива): `X-API-Version: 1`

### Authentication

- JWT Authorization header: `Authorization: Bearer <token>`
- Входящие запросы проверяются API Gateway
- Исходящие вызовы между сервисами передают JWT токен

### Rate Limiting

- API Gateway применяет rate limiting к входящим запросам
- Пределы настроены в зависимости от типа запроса
- Превышение лимита возвращает `429 Too Many Requests`

### Статус коды

| Код | Описание |
|-----|----------|
| 200 | Успешный GET/PUT/PATCH |
| 201 | Успешное создание (POST) |
| 202 | Запрос принят на обработку (асинхронно) |
| 204 | Успешное удаление (DELETE) |
| 400 | Невалидные данные |
| 401 | Неавторизован |
| 403 | Запрещено |
| 404 | Не найдено |
| 409 | Конфликт |
| 429 | Слишком много запросов |
| 500 | Внутренняя ошибка сервера |
| 503 | Сервис недоступен |

---

## Лучшие практики

### Именование

- **Endpoints:** snake_case (`/api/v1/users/profile`)
- **Parameters:** snake_case
- **Headers:** PascalCase (`X-Request-ID`)
- **Response fields:** camelCase

### Ошибки

```json
{
  "error": "ValidationError",
  "message": "Invalid input data",
  "details": [
    {
      "field": "email",
      "message": "Invalid email format"
    }
  ],
  "timestamp": "2026-06-04T10:30:00Z"
}
```

### Пагинация

```json
{
  "content": [...],
  "totalElements": 100,
  "totalPages": 5,
  "number": 0,
  "size": 20
}
```

---

## Инструменты

- **OpenAPI Generator** - генерация клиентского и серверного кода
- **Swagger UI** - визуализация API спецификаций
- **Swagger Editor** - редактирование и валидация спецификаций
- **Postman** - тестирование API

---

## Поддержка

Для вопросов по API спецификациям обращайтесь к команде архитектуры.

---

## История изменений

| Версия | Дата | Изменения |
|--------|------|-----------|
| 2.0 | 2026-06-04 | Обновление к MVP: 8 сервисов (консолидация 25+ → 8) |
| 1.0 | 2026-06-04 | Initial release (25+ сервисов) |

---

## См. также

- `docs/architecture/service-consolidation.md` - детали консолидации сервисов
- `docs/architecture/system-overview.md` - обзор архитектуры
- `docs/architecture/service-catalog/` - описания сервисов
