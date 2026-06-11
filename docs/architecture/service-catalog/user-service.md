# User Service

**Версия документа:** 1.1  
**Дата создания:** 2026-06-03  
**Последнее обновление:** 2026-06-04

---

## Обзор

**Внимание:** Согласно консолидации сервисов для MVP, User Service был объединён в **Platform Service**. Этот документ оставлен для исторической справки.

До консолидации User Service отвечал за управление пользователями и профилями. После консолидации его функционал включён в Platform Service (см. `platform-service.md`).

---

## Историческая справка (до консолидации)

User Service был сервисом управления пользователями и профилями. Он хранит персональную информацию пользователей, управляет верификацией, настройками магазина продавца, программой лояльности, историей поиска и просмотров, избранным содержимым и персонализированными рекомендациями.

---

## Бизнес-функция (историческая)

User Service обеспечивал:
- Управление профилями пользователей
- Верификацию пользователей
- Настройки магазина для продавцов
- Мультивалютность и мультиязычность
- Программу лояльности с накоплением баллов
- Историю поиска и просмотров
- Избранное с папками по категориям
- Персонализированные рекомендации и скидки
- Синхронизацию с Keycloak через события Kafka

---

## Текущий статус

| Функция | Сервис после консолидации |
|--------|--------------------------|
| Управление профилями | Platform Service |
| Верификация | Platform Service |
| Настройки магазина | Platform Service |
| Лояльность | Platform Service |
| История поиска | Platform Service |
| Избранное | Platform Service |
| Рекомендации | Platform Service |

---

## Контакты

- **Owner:** Backend Team
- **Slack:** #user-service (историческая справка)
- **Emergency:** #incident

---

## См. также

- [Консолидация сервисов](../service-consolidation.md) — детали консолидации
- [Системный обзор](../system-overview.md) — обзор архитектуры
- [Platform Service](platform-service.md) — текущий сервис
- [ADR-0001](../architecture-decision-records/adr-0001-consolidation.md) — консолидация сервисов

---

## Бизнес-функция

User Service обеспечивает:
- Управление профилями пользователей
- Верификацию пользователей
- Настройки магазина для продавцов
- Мультивалютность и мультиязычность
- Программу лояльности с накоплением баллов
- Историю поиска и просмотров
- Избранное с папками по категориям
- Персонализированные рекомендации и скидки
- Синхронизацию с Keycloak через события Kafka

---

## API endpoints

### User Profiles

| Method | Path | Описание | RBAC |
|--------|------|----------|------|
| `GET` | `/api/v1/users/profile` | Получение профиля текущего пользователя | BUYER, SELLER |
| `PUT` | `/api/v1/users/profile` | Обновление профиля текущего пользователя | BUYER, SELLER |
| `GET` | `/api/v1/users/profiles/{userId}` | Получение профиля по ID | BUYER, SELLER |
| `POST` | `/api/v1/users/verify` | Запрос верификации | BUYER, SELLER |

### Store Settings (для продавцов)

| Method | Path | Описание | RBAC |
|--------|------|----------|------|
| `GET` | `/api/v1/users/store/settings` | Получение настроек магазина | SELLER |
| `PUT` | `/api/v1/users/store/settings` | Обновление настроек магазина | SELLER |
| `POST` | `/api/v1/users/store/verify` | Запрос верификации магазина | SELLER |

### Loyalty Program

| Method | Path | Описание | RBAC |
|--------|------|----------|------|
| `GET` | `/api/v1/users/loyalty/balance` | Получение баланса лояльности | BUYER, SELLER |
| `GET` | `/api/v1/users/loyalty/history` | Получение истории начислений | BUYER, SELLER |
| `POST` | `/api/v1/users/loyalty/redeem` | Использование баллов | BUYER |

### Search & View History

| Method | Path | Описание | RBAC |
|--------|------|----------|------|
| `GET` | `/api/v1/users/history/search` | Получение истории поиска | BUYER |
| `DELETE` | `/api/v1/users/history/search` | Очистка истории поиска | BUYER |
| `GET` | `/api/v1/users/history/views` | Получение истории просмотров | BUYER |
| `DELETE` | `/api/v1/users/history/views` | Очистка истории просмотров | BUYER |

### Favorites

| Method | Path | Описание | RBAC |
|--------|------|----------|------|
| `GET` | `/api/v1/users/favorites` | Получение избранного | BUYER |
| `POST` | `/api/v1/users/favorites` | Добавление в избранное | BUYER |
| `DELETE` | `/api/v1/users/favorites/{itemId}` | Удаление из избранного | BUYER |
| `GET` | `/api/v1/users/favorites/folders` | Получение папок избранного | BUYER |
| `POST` | `/api/v1/users/favorites/folders` | Создание папки избранного | BUYER |

### Personalization

| Method | Path | Описание | RBAC |
|--------|------|----------|------|
| `GET` | `/api/v1/users/personalization/recommendations` | Получение рекомендаций | BUYER |
| `GET` | `/api/v1/users/personalization/discounts` | Получение персонализированных скидок | BUYER |

---

## Технические детали

### Конфигурация (application.yml)
```yaml
server:
  port: 8083

spring:
  application:
    name: user-service
  datasource:
    driver-class-name: org.postgresql.Driver
    url: jdbc:postgresql://${USER_DB_HOST:localhost}:${USER_DB_PORT:5438}/${USER_DB_NAME:services}
    username: ${USER_DB_USER:postgres}
    password: ${USER_DB_PASS:postgres}
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

### Схема: `user_service`

#### Таблица: `user_profiles`
```sql
CREATE TABLE user_service.user_profiles (
    id                  BIGSERIAL      PRIMARY KEY,
    user_id             BIGINT         NOT NULL   UNIQUE,
    keycloak_user_id    VARCHAR(255)   NOT NULL,
    first_name          VARCHAR(255)   NULL,
    last_name           VARCHAR(255)   NULL,
    phone               VARCHAR(50)    NULL,
    email               VARCHAR(255)   NOT NULL,
    avatar_url          VARCHAR(255)   NULL,
    verified            BOOLEAN        NOT NULL   DEFAULT FALSE,
    created_at          TIMESTAMP      NOT NULL   DEFAULT CURRENT_TIMESTAMP,
    updated_at        	TIMESTAMP      NOT NULL   DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES auth.users(id)
);
```

#### Таблица: `store_settings`
```sql
CREATE TABLE user_service.store_settings (
    id                  BIGSERIAL      PRIMARY KEY,
    user_id             BIGINT         NOT NULL   UNIQUE,
    store_name          VARCHAR(255)   NULL,
    store_description   TEXT           NULL,
    store_logo_url      VARCHAR(255)   NULL,
    verification_status VARCHAR(50)    NOT NULL   DEFAULT 'pending',
    created_at          TIMESTAMP      NOT NULL   DEFAULT CURRENT_TIMESTAMP,
    updated_at        	TIMESTAMP      NOT NULL   DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES auth.users(id)
);
```

#### Таблица: `loyalty_accounts`
```sql
CREATE TABLE user_service.loyalty_accounts (
    id                  BIGSERIAL      PRIMARY KEY,
    user_id             BIGINT         NOT NULL   UNIQUE,
    balance             NUMERIC(10,2)  NOT NULL   DEFAULT 0,
    total_spent         NUMERIC(10,2)  NOT NULL   DEFAULT 0,
    tier                VARCHAR(50)    NOT NULL   DEFAULT 'bronze',
    created_at          TIMESTAMP      NOT NULL   DEFAULT CURRENT_TIMESTAMP,
    updated_at        	TIMESTAMP      NOT NULL   DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES auth.users(id)
);
```

#### Таблица: `search_history`
```sql
CREATE TABLE user_service.search_history (
    id                  BIGSERIAL      PRIMARY KEY,
    user_id             BIGINT         NOT NULL,
    query               TEXT           NOT NULL,
    results_count       INTEGER        NULL,
    created_at          TIMESTAMP      NOT NULL   DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES auth.users(id)
);
```

#### Таблица: `view_history`
```sql
CREATE TABLE user_service.view_history (
    id                  BIGSERIAL      PRIMARY KEY,
    user_id             BIGINT         NOT NULL,
    product_id          BIGINT         NOT NULL,
    viewed_at           TIMESTAMP      NOT NULL   DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES auth.users(id),
    FOREIGN KEY (product_id) REFERENCES catalog.products(id)
);
```

#### Таблица: `favorite_items`
```sql
CREATE TABLE user_service.favorite_items (
    id                  BIGSERIAL      PRIMARY KEY,
    user_id             BIGINT         NOT NULL,
    folder_id           BIGINT         NULL,
    product_id          BIGINT         NOT NULL,
    created_at          TIMESTAMP      NOT NULL   DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES auth.users(id),
    FOREIGN KEY (folder_id) REFERENCES user_service.favorite_folders(id),
    FOREIGN KEY (product_id) REFERENCES catalog.products(id)
);
```

#### Таблица: `favorite_folders`
```sql
CREATE TABLE user_service.favorite_folders (
    id                  BIGSERIAL      PRIMARY KEY,
    user_id             BIGINT         NOT NULL,
    name                VARCHAR(255)   NOT NULL,
    description         VARCHAR(255)   NULL,
    created_at          TIMESTAMP      NOT NULL   DEFAULT CURRENT_TIMESTAMP,
    updated_at        	TIMESTAMP      NOT NULL   DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES auth.users(id)
);
```

---

## Архитектурные решения

### Интеграция с Auth Service
- Слушает событие `auth.user_registered` через Kafka
- Создаёт профиль пользователя при регистрации
- Синхронизация с Keycloak user ID

### Кэширование в Redis
- Кэш профилей пользователей (1 час TTL)
- Кэш истории поиска (1 день TTL)
- Кэш персонализированных рекомендаций (1 час TTL)

### Лояльность
- **Модель:** Накопление баллов за покупки
- **Тиры:** Bronze (0-1000), Silver (1001-5000), Gold (5001+)
- **Начисление:** 1% от суммы покупки

### Персонализация
- На основе истории просмотров и поиска
- Collaboration filtering для рекомендаций
- А/B тестирование для скидок

---

## Паттерны проектирования

### Repository Pattern
Использование Spring Data JPA для доступа к данным.

### CQRS Pattern (частично)
- **Commands:** Обновление профиля, добавление в избранное
- **Queries:** Получение профиля, история поиска

### Event Sourcing
События Kafka используются для асинхронной синхронизации с другими сервисами.

---

## Межсервисное взаимодействие

### Синхронное (REST API)
- User Service → Auth Service (проверка пользователя)
- User Service → Catalog Service (получение товаров для истории)

### Асинхронное (Kafka)
- User Service → Kafka (события: user.profile_created, user.profile_updated)
- User Service ← Kafka (события: order.completed, payment.completed)
- User Service → Kafka (события: loyalty.points_earned, loyalty.tier_upgraded)

---

## Безопасность

### Аутентификация
- JWT токены в заголовке `Authorization: Bearer {token}`
- Проверка токена через Auth Service

### Авторизация
- Пользователь может редактировать только свой профиль
- Продавец может управлять только своим магазином
- ADMIN и MODERATOR имеют полный доступ

---

## Метрики

| Метрика | Описание | Тип |
|---------|----------|-----|
| `user_profile_operations_total` | Количество операций с профилями | Counter |
| `loyalty_points_earned_total` | Всего начислено баллов лояльности | Counter |
| `search_queries_total` | Количество поисковых запросов | Counter |
| `cache_hits_total` | Количество попаданий в кэш | Counter |
| `recommendation_requests_total` | Количество запросов рекомендаций | Counter |

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
  name: user-service
spec:
  replicas: 2
  template:
    spec:
      containers:
      - name: user-service
        image: autodev/user-service:latest
        ports:
        - containerPort: 8083
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
- Система лояльности
- История поиска и просмотров

### Integration Tests
- Testcontainers для PostgreSQL, Redis, Kafka

---

## Мониторинг

### Grafana Dashboard
- User Service Overview
- Profile Operations
- Loyalty Program Stats
- Search History

---

## Контакты

- **Owner:** Backend Team
- **Slack:** #user-service
