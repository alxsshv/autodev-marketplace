# Platform Service

**Версия документа:** 1.0  
**Дата создания:** 2026-06-04  
**Последнее обновление:** 2026-06-04

---

## Обзор

Platform Service — это центральный сервис управления платформой. Он объединяет все платформенные функции: управление пользователями и профилями, модерацию контента, отзывы и рейтинги, аналитику, администрирование и маркетинг. Это единый домен для управления всей платформой.

---

## Бизнес-функция

Platform Service обеспечивает:
- Управление профилями пользователей и продавцов
- Верификацию пользователей и магазинов
- Модерацию контента (объявления, отзывы)
- Управление отзывами и рейтингами
- Аналитику для пользователей и администраторов
- Настройку системы через админку
- Управление маркетинговыми кампаниями
- Интеграцию с сервисами коммуникации и уведомлений

---

## API endpoints

### User Profiles

| Method | Path | Описание | RBAC |
|--------|------|----------|------|
| `GET` | `/api/v1/platform/users/profile` | Получение профиля | BUYER, SELLER |
| `PUT` | `/api/v1/platform/users/profile` | Обновление профиля | BUYER, SELLER |
| `GET` | `/api/v1/platform/users/profiles/{userId}` | Получение профиля по ID | BUYER, SELLER |
| `POST` | `/api/v1/platform/users/verify` | Запрос верификации | BUYER, SELLER |

### Store Settings (для продавцов)

| Method | Path | Описание | RBAC |
|--------|------|----------|------|
| `GET` | `/api/v1/platform/users/store/settings` | Получение настроек магазина | SELLER |
| `PUT` | `/api/v1/platform/users/store/settings` | Обновление настроек | SELLER |
| `POST` | `/api/v1/platform/users/store/verify` | Запрос верификации магазина | SELLER |

### Moderation

| Method | Path | Описание | RBAC |
|--------|------|----------|------|
| `GET` | `/api/v1/platform/moderation/items` | Список модерируемых элементов | MODERATOR, ADMIN |
| `PUT` | `/api/v1/platform/moderation/items/{id}/approve` | Одобрить элемент | MODERATOR, ADMIN |
| `PUT` | `/api/v1/platform/moderation/items/{id}/reject` | Отклонить элемент | MODERATOR, ADMIN |
| `POST` | `/api/v1/platform/moderation/reports` | Подать жалобу | BUYER, SELLER |

### Reviews

| Method | Path | Описание | RBAC |
|--------|------|----------|------|
| `GET` | `/api/v1/platform/reviews` | Список отзывов | BUYER, SELLER |
| `POST` | `/api/v1/platform/reviews` | Оставить отзыв | BUYER |
| `PUT` | `/api/v1/platform/reviews/{id}` | Обновить отзыв | BUYER |
| `DELETE` | `/api/v1/platform/reviews/{id}` | Удалить отзыв | BUYER |
| `GET` | `/api/v1/platform/reviews/products/{productId}` | Отзывы о товаре | BUYER |

### Analytics

| Method | Path | Описание | RBAC |
|--------|------|----------|------|
| `GET` | `/api/v1/platform/analytics/dashboard` | Дашборд аналитики | ADMIN |
| `GET` | `/api/v1/platform/analytics/users` | Аналитика пользователей | ADMIN |
| `GET` | `/api/v1/platform/analytics/sales` | Аналитика продаж | ADMIN |
| `GET` | `/api/v1/platform/analytics/categories` | Аналитика по категориям | ADMIN |
| `GET` | `/api/v1/platform/analytics/seller/{sellerId}` | Аналитика продавца | SELLER |

### Admin

| Method | Path | Описание | RBAC |
|--------|------|----------|------|
| `GET` | `/api/v1/platform/admin/settings` | Получить настройки | ADMIN |
| `PUT` | `/api/v1/platform/admin/settings` | Обновить настройки | ADMIN |
| `GET` | `/api/v1/platform/admin/audit` | Аудит логи | ADMIN |
| `GET` | `/api/v1/platform/admin/config` | Конфигурация системы | ADMIN |

### Marketing

| Method | Path | Описание | RBAC |
|--------|------|----------|------|
| `GET` | `/api/v1/platform/marketing/promotions` | Акции и скидки | BUYER, SELLER |
| `POST` | `/api/v1/platform/marketing/promotions` | Создать акцию | ADMIN |
| `GET` | `/api/v1/platform/marketing/banners` | Баннеры | BUYER |
| `POST` | `/api/v1/platform/marketing/banners` | Создать баннер | ADMIN |

---

## Технические детали

### Конфигурация (application.yml)
```yaml
server:
  port: 8089

spring:
  application:
    name: platform-service
  datasource:
    driver-class-name: org.postgresql.Driver
    url: jdbc:postgresql://${PLATFORM_DB_HOST:localhost}:${PLATFORM_DB_PORT:5438}/${PLATFORM_DB_NAME:services}
    username: ${PLATFORM_DB_USER:postgres}
    password: ${PLATFORM_DB_PASS:postgres}
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
```

### Зависимости (build.gradle.kts)
```kotlin
dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
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

### Схема: `platform_service`

#### Таблица: `users`
```sql
CREATE TABLE platform_service.users (
    id                  BIGSERIAL      PRIMARY KEY,
    keycloak_user_id    VARCHAR(255)   NOT NULL   UNIQUE,
    email               VARCHAR(255)   NOT NULL   UNIQUE,
    first_name          VARCHAR(255)   NULL,
    last_name           VARCHAR(255)   NULL,
    phone               VARCHAR(50)    NULL,
    role                VARCHAR(50)    NOT NULL,
    verified            BOOLEAN        NOT NULL   DEFAULT FALSE,
    created_at          TIMESTAMP      NOT NULL   DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP      NOT NULL   DEFAULT CURRENT_TIMESTAMP
);
```

#### Таблица: `user_profiles`
```sql
CREATE TABLE platform_service.user_profiles (
    id                  BIGSERIAL      PRIMARY KEY,
    user_id             BIGINT         NOT NULL   UNIQUE,
    avatar_url          VARCHAR(255)   NULL,
    verified            BOOLEAN        NOT NULL   DEFAULT FALSE,
    created_at          TIMESTAMP      NOT NULL   DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP      NOT NULL   DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES platform_service.users(id)
);
```

#### Таблица: `store_settings`
```sql
CREATE TABLE platform_service.store_settings (
    id                  BIGSERIAL      PRIMARY KEY,
    user_id             BIGINT         NOT NULL   UNIQUE,
    store_name          VARCHAR(255)   NULL,
    store_description   TEXT           NULL,
    store_logo_url      VARCHAR(255)   NULL,
    verification_status VARCHAR(50)    NOT NULL   DEFAULT 'pending',
    created_at          TIMESTAMP      NOT NULL   DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP      NOT NULL   DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES platform_service.users(id)
);
```

#### Таблица: `moderation_items`
```sql
CREATE TABLE platform_service.moderation_items (
    id                  BIGSERIAL      PRIMARY KEY,
    item_type           VARCHAR(50)    NOT NULL,
    item_id             BIGINT         NOT NULL,
    status              VARCHAR(50)    NOT NULL,
    moderator_id        BIGINT         NULL,
    rejection_reason    TEXT           NULL,
    created_at          TIMESTAMP      NOT NULL   DEFAULT CURRENT_TIMESTAMP,
    reviewed_at         TIMESTAMP      NULL,
    FOREIGN KEY (moderator_id) REFERENCES platform_service.users(id)
);
```

#### Таблица: `reviews`
```sql
CREATE TABLE platform_service.reviews (
    id                  BIGSERIAL      PRIMARY KEY,
    product_id          BIGINT         NOT NULL,
    user_id             BIGINT         NOT NULL,
    order_id            BIGINT         NULL,
    rating              INTEGER        NOT NULL,
    title               VARCHAR(255)   NULL,
    content             TEXT           NULL,
    images              JSONB          NULL,
    is_verified_purchase BOOLEAN       NOT NULL   DEFAULT FALSE,
    created_at          TIMESTAMP      NOT NULL   DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP      NOT NULL   DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (product_id) REFERENCES catalog.products(id),
    FOREIGN KEY (user_id) REFERENCES platform_service.users(id),
    FOREIGN KEY (order_id) REFERENCES order_service.orders(id)
);
```

#### Таблица: `analytics`
```sql
CREATE TABLE platform_service.analytics (
    id                  BIGSERIAL      PRIMARY KEY,
    metric_name         VARCHAR(100)   NOT NULL,
    metric_value        NUMERIC(15,2)  NOT NULL,
    dimension           VARCHAR(100)   NULL,
    date                DATE           NOT NULL,
    created_at          TIMESTAMP      NOT NULL   DEFAULT CURRENT_TIMESTAMP
);
```

---

## Архитектурные решения

### Модульность
- **Пакеты Java:** user, moderation, review, analytics, admin, marketing
- **Один сервис, несколько модулей** для лучшей структуры

### Event Sourcing
- События Kafka для асинхронной синхронизации с другими сервисами
- `platform.user.registered`, `platform.review.created`, `platform.moderation.approved`

### Кэширование
- **Redis:** Профили пользователей (1 час), Модерируемые элементы (15 минут)
- **Повышение производительности:** Уменьшение нагрузки на БД

### Роли и права
- **Роли:** BUYER, SELLER, MODERATOR, ADMIN
- **Permissions:** Детальный контроль доступа
- **RBAC:** Role-Based Access Control

---

## Паттерны проектирования

### Repository Pattern
Использование Spring Data JPA для доступа к данным.

### Event Sourcing
События Kafka используются для асинхронной обработки всех платформенных функций.

### Facade Pattern
Единый API для всех платформенных функций.

---

## Межсервисное взаимодействие

### Синхронное (REST API)
- Platform Service → Auth Service (проверка пользователя)
- Platform Service → Catalog Service (получение товаров для отзывов)
- Platform Service → Order Service (проверка покупки для отзыва)

### Асинхронное (Kafka)
- Platform Service → Kafka (события: user.registered, review.created, moderation.approved)
- Platform Service ← Kafka (события: order.completed, payment.completed)

---

## Безопасность

### Аутентификация
- JWT токены в заголовке `Authorization: Bearer {token}`
- Проверка токена через Auth Service

### Авторизация
- BUYER: Просмотр товаров, оставление отзывов
- SELLER: Управление своим магазином, ответы на отзывы
- MODERATOR: Модерация контента
- ADMIN: Полный доступ к системе

---

## Метрики

| Метрика | Описание | Тип |
|---------|----------|-----|
| `platform_user_operations_total` | Количество операций с пользователями | Counter |
| `platform_moderation_items_total` | Количество модерируемых элементов | Counter |
| `platform_reviews_created_total` | Количество созданных отзывов | Counter |
| `platform_analytics_calculations_total` | Количество расчётов аналитики | Counter |

---

## SLA/SLO

| Метрика | Целевое значение | Измерение |
|---------|-----------------|-----------|
| Доступность | 99.9% | Uptime (Prometheus) |
| Latency (p95) | <100 мс | Tempo traces |
| Latency (p99) | <300 мс | Tempo traces |
| Ошибки (p99) | <1% | Prometheus errors |

---

## Deployment

### Kubernetes
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: platform-service
spec:
  replicas: 2
  template:
    spec:
      containers:
      - name: platform-service
        image: autodev/platform-service:latest
        ports:
        - containerPort: 8089
        resources:
          requests:
            memory: "256Mi"
            cpu: "250m"
          limits:
            memory: "512Mi"
            cpu: "500m"
```

---

## Тестирование

### Unit Tests
- Управление профилями
- Модерация контента
- Управление отзывами
- Аналитика

### Integration Tests
- Testcontainers для PostgreSQL, Redis, Kafka
- Симуляция событий от других сервисов

---

## Мониторинг

### Grafana Dashboard
- Platform Service Overview
- User Management
- Moderation Queue
- Review Statistics
- Analytics Dashboard

---

## Риски и ограничения

### Текущие риски
1. **Service size** — большой сервис с множеством функций
   - **Mitigation:** Модульность, чёткое разделение ответственностей

2. **Event ordering** — порядок событий Kafka может быть нарушен
   - **Mitigation:** Использование correlation ID, обработка дубликатов

3. **Analytics computation** — сложные расчёты аналитики
   - **Mitigation:** Асинхронные расчёты, кэширование результатов

---

## План улучшений

### Short-term (1-2 недели)
- [ ] Реализовать модерацию контента
- [ ] Интеграция с отзывами
- [ ] Базовая аналитика

### Medium-term (1-2 месяца)
- [ ] Продвинутая аналитика для продавцов
- [ ] Админка для управления системой
- [ ] Маркетинговые кампании

### Long-term (3-6 месяцев)
- [ ] AI-модерация
- [ ] Автоматические рекламные кампании
- [ ] Продвинутая аналитика ML

---

## Контакты

- **Owner:** Backend Team
- **Slack:** #platform-service
- **Emergency:** #incident

---

## См. также

- [Консолидация сервисов](../service-consolidation.md) — детали консолидации
- [Системный обзор](../system-overview.md) — обзор архитектуры
- [OpenAPI спецификация](../api-specification/README.md) — API документация
- [ADR-0001](../architecture-decision-records/adr-0001-consolidation.md) — консолидация сервисов
