# Catalog Service

**Версия документа:** 1.0  
**Дата создания:** 2026-06-03  
**Последнее обновление:** 2026-06-03

---

## Обзор

Catalog Service — это сервис управления каталогом товаров. Он предоставляет расширенный функционал для каталога автозапчастей с подбором по VIN, каталогом аналогов, управлением категориями, характеристиками и совместимостью. Интегрируется с внешними каталогами (TecDoc) для получения данных о совместимости автомобилей.

---

## Бизнес-функция

Catalog Service обеспечивает:
- Управление каталогом товаров (продукты)
- Управление категориями и подкатегориями
- Подбор запчастей по VIN-коду автомобиля
- Каталог аналогов и заменителей
- Управление совместимостью по VIN
- Кросс-номера и альтернативные обозначения
- Синхронизацию с внешними каталогами (TecDoc)
- Интеграцию с Elasticsearch для полнотекстового поиска

---

## API endpoints

### Products

| Method | Path | Описание | RBAC |
|--------|------|----------|------|
| `GET` | `/api/v1/catalog/products` | Список товаров с фильтрацией | BUYER, SELLER |
| `GET` | `/api/v1/catalog/products/{id}` | Получение товара по ID | BUYER, SELLER |
| `POST` | `/api/v1/catalog/products` | Создание товара | SELLER |
| `PUT` | `/api/v1/catalog/products/{id}` | Обновление товара | SELLER |
| `DELETE` | `/api/v1/catalog/products/{id}` | Удаление товара | SELLER |

### Categories

| Method | Path | Описание | RBAC |
|--------|------|----------|------|
| `GET` | `/api/v1/catalog/categories` | Список всех категорий | BUYER, SELLER |
| `GET` | `/api/v1/catalog/categories/{id}` | Получение категории по ID | BUYER, SELLER |
| `POST` | `/api/v1/catalog/categories` | Создание категории | ADMIN |
| `PUT` | `/api/v1/catalog/categories/{id}` | Обновление категории | ADMIN |

### VIN Lookup

| Method | Path | Описание | RBAC |
|--------|------|----------|------|
| `GET` | `/api/v1/catalog/vin/{vin}/parts` | Подбор запчастей по VIN | BUYER |
| `GET` | `/api/v1/catalog/vin/{vin}/vehicles` | Получение данных авто по VIN | BUYER |
| `GET` | `/api/v1/catalog/vin/{vin}/compatibility` | Проверка совместимости | BUYER |

### Alternatives

| Method | Path | Описание | RBAC |
|--------|------|----------|------|
| `GET` | `/api/v1/catalog/products/{id}/alternatives` | Получение аналогов товара | BUYER |
| `POST` | `/api/v1/catalog/products/{id}/alternatives` | Добавление аналога | ADMIN |

### Cross References

| Method | Path | Описание | RBAC |
|--------|------|----------|------|
| `GET` | `/api/v1/catalog/products/{id}/cross-references` | Получение кросс-номеров | BUYER |
| `POST` | `/api/v1/catalog/products/{id}/cross-references` | Добавление кросс-номера | ADMIN |

---

## Технические детали

### Конфигурация (application.yml)
```yaml
server:
  port: 8084

spring:
  application:
    name: catalog-service
  datasource:
    driver-class-name: org.postgresql.Driver
    url: jdbc:postgresql://${CATALOG_DB_HOST:localhost}:${CATALOG_DB_PORT:5438}/${CATALOG_DB_NAME:services}
    username: ${CATALOG_DB_USER:postgres}
    password: ${CATALOG_DB_PASS:postgres}
  elasticsearch:
    uris: http://${ELASTICSEARCH_HOST:localhost}:${ELASTICSEARCH_PORT:9200}
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
    implementation("org.springframework.boot:spring-boot-starter-data-elasticsearch")
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

### Схема: `catalog`

#### Таблица: `products`
```sql
CREATE TABLE catalog.products (
    id                  BIGSERIAL      PRIMARY KEY,
    sku                 VARCHAR(255)   NOT NULL   UNIQUE,
    name                VARCHAR(255)   NOT NULL,
    description         TEXT           NULL,
    brand_id            BIGINT         NULL,
    category_id         BIGINT         NOT NULL,
    price               NUMERIC(10,2)  NOT NULL,
    currency            VARCHAR(3)     NOT NULL   DEFAULT 'RUB',
    condition           VARCHAR(50)    NOT NULL   DEFAULT 'new',
    vin_compatibility   JSONB          NULL,
    created_at          TIMESTAMP      NOT NULL   DEFAULT CURRENT_TIMESTAMP,
    updated_at        	TIMESTAMP      NOT NULL   DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES catalog.categories(id),
    FOREIGN KEY (brand_id) REFERENCES catalog.brands(id)
);
```

#### Таблица: `categories`
```sql
CREATE TABLE catalog.categories (
    id                  BIGSERIAL      PRIMARY KEY,
    name                VARCHAR(255)   NOT NULL,
    slug                VARCHAR(255)   NOT NULL   UNIQUE,
    description         TEXT           NULL,
    parent_id           BIGINT         NULL,
    created_at          TIMESTAMP      NOT NULL   DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (parent_id) REFERENCES catalog.categories(id)
);
```

#### Таблица: `brands`
```sql
CREATE TABLE catalog.brands (
    id                  BIGSERIAL      PRIMARY KEY,
    name                VARCHAR(255)   NOT NULL   UNIQUE,
    logo_url            VARCHAR(255)   NULL,
    created_at          TIMESTAMP      NOT NULL   DEFAULT CURRENT_TIMESTAMP
);
```

#### Таблица: `product_characteristics`
```sql
CREATE TABLE catalog.product_characteristics (
    id                  BIGSERIAL      PRIMARY KEY,
    product_id          BIGINT         NOT NULL,
    name                VARCHAR(255)   NOT NULL,
    value               VARCHAR(255)   NOT NULL,
    FOREIGN KEY (product_id) REFERENCES catalog.products(id)
);
```

#### Таблица: `vin_compatibility`
```sql
CREATE TABLE catalog.vin_compatibility (
    id                  BIGSERIAL      PRIMARY KEY,
    product_id          BIGINT         NOT NULL,
    vin                 VARCHAR(17)    NOT NULL,
    vehicle_name        VARCHAR(255)   NOT NULL,
    created_at          TIMESTAMP      NOT NULL   DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (product_id) REFERENCES catalog.products(id)
);
```

#### Таблица: `alternatives`
```sql
CREATE TABLE catalog.alternatives (
    id                  BIGSERIAL      PRIMARY KEY,
    product_id          BIGINT         NOT NULL,
    alternative_id      BIGINT         NOT NULL,
    created_at          TIMESTAMP      NOT NULL   DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (product_id) REFERENCES catalog.products(id),
    FOREIGN KEY (alternative_id) REFERENCES catalog.products(id)
);
```

#### Таблица: `cross_references`
```sql
CREATE TABLE catalog.cross_references (
    id                  BIGSERIAL      PRIMARY KEY,
    product_id          BIGINT         NOT NULL,
    cross_reference     VARCHAR(255)   NOT NULL,
    manufacturer        VARCHAR(255)   NULL,
    created_at          TIMESTAMP      NOT NULL   DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (product_id) REFERENCES catalog.products(id)
);
```

---

## Архитектурные решения

### Интеграция с Elasticsearch
- **Использование:** Полнотекстовый поиск товаров
- **Индексы:** `products`, `categories`, `brands`
- **Синхронизация:** Асинхронная через Kafka (событие product.created/updated/deleted)

### Интеграция с TecDoc
- **Использование:** Данные о совместимости автомобилей
- **API:** REST API TecDoc
- **Данные:** VIN → vehicle → parts compatibility

### VIN Lookup
- **Разбор VIN:** Автоматический разбор VIN-кода
- **Совместимость:** Проверка совместимости запчастей с авто
- **Схемы:** Отображение схемы автомобиля с номерами деталей

### Импорт прайс-листов
- **Форматы:** CSV, XLSX, XML, YML
- **Поток:** Upload → Validation → Import → Event
- **Режимы:** Полная замена, Добавление, Обновление

---

## Паттерны проектирования

### Repository Pattern
Использование Spring Data JPA для доступа к данным.

### Event Sourcing
События Kafka используются для синхронизации с Elasticsearch и другими сервисами.

### Data Transfer Object (DTO)
Использование DTO для передачи данных между слоями.

---

## Межсервисное взаимодействие

### Синхронное (REST API)
- Catalog Service → Elasticsearch API (поиск)
- Catalog Service → TecDoc API (совместимость)

### Асинхронное (Kafka)
- Catalog Service → Kafka (события: product.created, product.updated, product.deleted)
- Catalog Service ← Kafka (события: pricing.price_updated, inventory.stock_updated)

---

## Безопасность

### Аутентификация
- JWT токены в заголовке `Authorization: Bearer {token}`
- Проверка токена через Auth Service

### Авторизация
- Публичный доступ к просмотру товаров
- SELLER может управлять только своими товарами
- ADMIN имеет полный доступ

---

## Метрики

| Метрика | Описание | Тип |
|---------|----------|-----|
| `catalog_product_operations_total` | Количество операций с товарами | Counter |
| `catalog_search_queries_total` | Количество поисковых запросов | Counter |
| `catalog_vin_lookups_total` | Количество подборов по VIN | Counter |
| `elasticsearch_queries_total` | Количество запросов к Elasticsearch | Counter |
| `tecdoc_api_calls_total` | Количество вызовов TecDoc API | Counter |

---

## SLA/SLO

| Метрика | Целевое значение | Измерение |
|---------|-----------------|-----------|
| Доступность | 99.9% | Uptime (Prometheus) |
| Latency (p95) | <200 мс | Tempo traces |
| Latency (p99) | <500 мс | Tempo traces |
| Ошибки (p99) | <1% | Prometheus errors |

---

## Deployment

### Kubernetes
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: catalog-service
spec:
  replicas: 2
  template:
    spec:
      containers:
      - name: catalog-service
        image: autodev/catalog-service:latest
        ports:
        - containerPort: 8084
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
- Управление товарами
- Подбор по VIN
- Импорт прайс-листов

### Integration Tests
- Testcontainers для PostgreSQL, Elasticsearch, Kafka

---

## Мониторинг

### Grafana Dashboard
- Catalog Service Overview
- Product Operations
- Search Queries
- VIN Lookup Stats

---

## Контакты

- **Owner:** Backend Team
- **Slack:** #catalog-service

---

## См. также

- [Консолидация сервисов](../service-consolidation.md) — детали консолидации
- [Системный обзор](../system-overview.md) — обзор архитектуры
- [OpenAPI спецификация](../api-specification/README.md) — API документация
- [ADR-0001](../architecture-decision-records/adr-0001-consolidation.md) — консолидация сервисов
