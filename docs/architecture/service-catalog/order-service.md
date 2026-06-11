# Order Service

**Версия документа:** 1.0  
**Дата создания:** 2026-06-04  
**Последнее обновление:** 2026-06-04

---

## Обзор

Order Service — это сервис управления заказами, доставкой и возвратами. Он охватывает полный жизненный цикл покупки: от создания корзины и оформления заказа до доставки, отслеживания и возврата товара.

---

## Бизнес-функция

Order Service обеспечивает:
- Управление корзиной покупок
- Оформление заказов с эскроу-счётом
- Бронирование товара с резервированием наличия
- Выбор способов доставки с календарем
- Отслеживание статуса заказа с интеграцией ТК
- Возвраты и гарантийное обслуживание
- Согласование через Saga Pattern с другими сервисами
- Печать документов (накладные, счета)

---

## API endpoints

### Cart Management

| Method | Path | Описание | RBAC |
|--------|------|----------|------|
| `GET` | `/api/v1/orders/cart` | Получение текущей корзины | BUYER |
| `POST` | `/api/v1/orders/cart/items` | Добавление товара в корзину | BUYER |
| `PUT` | `/api/v1/orders/cart/items/{itemId}` | Обновление количества | BUYER |
| `DELETE` | `/api/v1/orders/cart/items/{itemId}` | Удаление товара из корзины | BUYER |
| `DELETE` | `/api/v1/orders/cart` | Очистка корзины | BUYER |

### Order Management

| Method | Path | Описание | RBAC |
|--------|------|----------|------|
| `GET` | `/api/v1/orders` | Список заказов пользователя | BUYER, SELLER |
| `GET` | `/api/v1/orders/{id}` | Получение заказа по ID | BUYER, SELLER |
| `POST` | `/api/v1/orders` | Оформление заказа | BUYER |
| `PUT` | `/api/v1/orders/{id}` | Обновление заказа | BUYER |
| `DELETE` | `/api/v1/orders/{id}` | Отмена заказа | BUYER |

### Order Status

| Method | Path | Описание | RBAC |
|--------|------|----------|------|
| `GET` | `/api/v1/orders/{id}/status` | Получение статуса заказа | BUYER, SELLER |
| `PUT` | `/api/v1/orders/{id}/status` | Обновление статуса заказа | SELLER, ADMIN |
| `GET` | `/api/v1/orders/{id}/timeline` | Хронология статусов заказа | BUYER, SELLER |

### Delivery

| Method | Path | Описание | RBAC |
|--------|------|----------|------|
| `GET` | `/api/v1/orders/delivery/options` | Список доступных способов доставки | BUYER |
| `GET` | `/api/v1/orders/delivery/calculator` | Расчёт стоимости доставки | BUYER |
| `GET` | `/api/v1/orders/delivery/track` | Отслеживание заказа с ТК | BUYER |
| `POST` | `/api/v1/orders/delivery/booking` | Бронирование даты доставки | BUYER |

### Returns

| Method | Path | Описание | RBAC |
|--------|------|----------|------|
| `POST` | `/api/v1/orders/returns` | Создание возврата | BUYER |
| `GET` | `/api/v1/orders/returns/{id}` | Получение возврата по ID | BUYER |
| `PUT` | `/api/v1/orders/returns/{id}` | Обновление возврата | BUYER |
| `GET` | `/api/v1/orders/returns` | Список возвратов | BUYER |

---

## Технические детали

### Конфигурация (application.yml)
```yaml
server:
  port: 8085

spring:
  application:
    name: order-service
  datasource:
    driver-class-name: org.postgresql.Driver
    url: jdbc:postgresql://${ORDER_DB_HOST:localhost}:${ORDER_DB_PORT:5438}/${ORDER_DB_NAME:services}
    username: ${ORDER_DB_USER:postgres}
    password: ${ORDER_DB_PASS:postgres}
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
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.cloud:spring-cloud-starter-consul-discovery")
    implementation("org.springframework.cloud:spring-cloud-starter-consul-config")
    implementation("org.springframework.kafka:spring-kafka")
    implementation("io.github.resilience4j:resilience4j-spring-boot3")
    
    implementation("org.postgresql:postgresql")
    implementation("org.liquibase:liquibase-core")
    
    implementation("io.micrometer:micrometer-registry-prometheus")
}
```

---

## Модель данных

### Схема: `order_service`

#### Таблица: `orders`
```sql
CREATE TABLE order_service.orders (
    id                  BIGSERIAL      PRIMARY KEY,
    user_id             BIGINT         NOT NULL,
    cart_id             BIGINT         NOT NULL,
    status              VARCHAR(50)    NOT NULL,
    total_amount        NUMERIC(10,2)  NOT NULL,
    escrow_amount       NUMERIC(10,2)  NOT NULL,
    delivery_address    JSONB          NOT NULL,
    created_at          TIMESTAMP      NOT NULL   DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP      NOT NULL   DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES platform_service.users(id)
);
```

#### Таблица: `order_items`
```sql
CREATE TABLE order_service.order_items (
    id                  BIGSERIAL      PRIMARY KEY,
    order_id            BIGINT         NOT NULL,
    product_id          BIGINT         NOT NULL,
    quantity            INTEGER        NOT NULL,
    price               NUMERIC(10,2)  NOT NULL,
    FOREIGN KEY (order_id) REFERENCES order_service.orders(id),
    FOREIGN KEY (product_id) REFERENCES catalog.products(id)
);
```

#### Таблица: `deliveries`
```sql
CREATE TABLE order_service.deliveries (
    id                  BIGSERIAL      PRIMARY KEY,
    order_id            BIGINT         NOT NULL UNIQUE,
    delivery_type       VARCHAR(50)    NOT NULL,
    carrier_id          VARCHAR(255)   NULL,
    tracking_number     VARCHAR(255)   NULL,
    estimated_delivery  TIMESTAMP      NULL,
    actual_delivery     TIMESTAMP      NULL,
    status              VARCHAR(50)    NOT NULL,
    created_at          TIMESTAMP      NOT NULL   DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES order_service.orders(id)
);
```

#### Таблица: `returns`
```sql
CREATE TABLE order_service.returns (
    id                  BIGSERIAL      PRIMARY KEY,
    order_id            BIGINT         NOT NULL,
    return_reason       VARCHAR(255)   NOT NULL,
    return_status       VARCHAR(50)    NOT NULL,
    refund_amount       NUMERIC(10,2)  NOT NULL,
    created_at          TIMESTAMP      NOT NULL   DEFAULT CURRENT_TIMESTAMP,
    processed_at        TIMESTAMP      NULL,
    FOREIGN KEY (order_id) REFERENCES order_service.orders(id)
);
```

---

## Архитектурные решения

### Saga Pattern
Использование Saga Pattern для координации распределённых транзакций:
- **Order Creation Saga:** Заказ → Резервирование → Оплата → Доставка
- **Return Saga:** Заявка на возврат → Проверка → Возврат средств

### События Kafka
- `order.order.created` — создание заказа
- `order.order.updated` — обновление заказа
- `order.order.completed` — завершение заказа
- `order.order.cancelled` — отмена заказа
- `order.delivery.updated` — обновление доставки
- `order.return.processed` — обработка возврата

### Резервирование наличия
- Асинхронное событие `inventory.reserve` через Kafka
- Отмена резерва через `inventory.release`
- Таймаут резервации (15 минут)

### Эскроу-счёт
- Блокировка средств на счёте при оформлении заказа
- Перевод средств продавцу после подтверждения доставки
- Возврат средств при отмене заказа

---

## Паттерны проектирования

### Repository Pattern
Использование Spring Data JPA для доступа к данным.

### Saga Pattern
Координация распределённых транзакций между сервисами.

### CQRS Pattern (частично)
- **Commands:** Оформление заказа, обновление статуса
- **Queries:** Получение заказов, статусов

---

## Межсервисное взаимодействие

### Синхронное (REST API)
- Order Service → Catalog Service (проверка наличия)
- Order Service → Payment Service (обработка оплаты)
- Order Service → Communication Service (уведомления)

### Асинхронное (Kafka)
- Order Service → Kafka (события заказа)
- Order Service ← Kafka (события: inventory.reserved, payment.completed)

---

## Статусы заказа

| Статус | Описание |
|--------|----------|
| `PENDING` | Ожидает подтверждения |
| `CONFIRMED` | Подтверждён продавцом |
| `PROCESSING` | В обработке |
| `READY_TO_SHIP` | Готов к отправке |
| `SHIPPED` | Отправлен |
| `IN_TRANSIT` | В доставке |
| `OUT_FOR_DELIVERY` | Доставляется |
| `DELIVERED` | Доставлен |
| `DELIVERY_ATTEMPTED` | Попытка доставки |
| `DELIVERY_SUCCESS` | Успешная доставка |
| `CANCELLED` | Отменён |
| `RETURNED` | Возвращён |

---

## Безопасность

### Аутентификация
- JWT токены в заголовке `Authorization: Bearer {token}`
- Проверка токена через Auth Service

### Авторизация
- Пользователь может управлять только своими заказами
- Продавец может видеть заказы своих товаров
- ADMIN и MODERATOR имеют полный доступ

---

## Метрики

| Метрика | Описание | Тип |
|---------|----------|-----|
| `order_created_total` | Количество созданных заказов | Counter |
| `order_completed_total` | Количество завершённых заказов | Counter |
| `order_cancelled_total` | Количество отменённых заказов | Counter |
| `delivery_operations_total` | Количество операций с доставкой | Counter |
| `return_operations_total` | Количество операций с возвратами | Counter |

---

## SLA/SLO

| Метрика | Целевое значение | Измерение |
|---------|-----------------|-----------|
| Доступность | 99.9% | Uptime (Prometheus) |
| Latency (p95) | <300 мс | Tempo traces |
| Latency (p99) | <800 мс | Tempo traces |
| Ошибки (p99) | <1% | Prometheus errors |

---

## Deployment

### Kubernetes
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: order-service
spec:
  replicas: 2
  template:
    spec:
      containers:
      - name: order-service
        image: autodev/order-service:latest
        ports:
        - containerPort: 8085
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
- Управление корзиной
- Оформление заказа
- Сценарии Saga Pattern

### Integration Tests
- Testcontainers для PostgreSQL, Redis, Kafka
- Симуляция событий от других сервисов

---

## Мониторинг

### Grafana Dashboard
- Order Service Overview
- Order Lifecycle
- Delivery Tracking
- Return Processing

---

## Риски и ограничения

### Текущие риски
1. **Saga complexity** — координация через 3+ сервиса может быть сложной
   - **Mitigation:** Использование Spring State Machine для управления состояниями

2. **Inventory reservation timeout** — резерв может истечь до оформления заказа
   - **Mitigation:** Уведомление пользователя о истечении резервации

3. **Payment failure** — неудача при оплате после резервирования
   - **Mitigation:** Автоматическое освобождение резерва

---

## План улучшений

### Short-term (1-2 недели)
- [ ] Реализовать полный Saga Pattern
- [ ] Добавить уведомления о статусах заказа
- [ ] Интеграция с службами доставки (СДЭК, Boxberry)

### Medium-term (1-2 месяца)
- [ ] Отслеживание по GPS
- [ ] Калькулятор стоимости доставки
- [ ] Гарантийное обслуживание

### Long-term (3-6 месяцев)
- [ ] Крипто-платежи
- [ ] NFT-сертификаты на товары
- [ ] Продвинутая аналитика заказов

---

## Контакты

- **Owner:** Backend Team
- **Slack:** #order-service
- **Emergency:** #incident

---

## См. также

- [Консолидация сервисов](../service-consolidation.md) — детали консолидации
- [Системный обзор](../system-overview.md) — обзор архитектуры
- [OpenAPI спецификация](../api-specification/README.md) — API документация
- [ADR-0001](../architecture-decision-records/adr-0001-consolidation.md) — консолидация сервисов
