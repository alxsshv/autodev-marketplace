# Communication Service

**Версия документа:** 1.0  
**Дата создания:** 2026-06-04  
**Последнее обновление:** 2026-06-04

---

## Обзор

Communication Service — это сервис управления коммуникацией между пользователями платформы. Он объединяет функционал уведомлений (email, SMS, push) и внутреннего чата в реальном времени, обеспечивая единый интерфейс для всех каналов взаимодействия.

---

## Бизнес-функция

Communication Service обеспечивает:
- Отправку email, SMS и push уведомлений
- Внутренний чат в реальном времени между покупателями и продавцами
- Управление подписками на события (снижение цены, статус заказа и т.д.)
- Настройку частоты и типов уведомлений
- Историю переписки и уведомлений
- Прикрепление файлов к сообщениям
- Шаблоны быстрых ответов
- Видеозвонки между пользователями

---

## API endpoints

### Notifications

| Method | Path | Описание | RBAC |
|--------|------|----------|------|
| `GET` | `/api/v1/notifications` | Список уведомлений | BUYER, SELLER |
| `GET` | `/api/v1/notifications/{id}` | Получение уведомления | BUYER, SELLER |
| `PUT` | `/api/v1/notifications/{id}/read` | Пометить как прочитанное | BUYER, SELLER |
| `DELETE` | `/api/v1/notifications/{id}` | Удалить уведомление | BUYER, SELLER |
| `POST` | `/api/v1/notifications` | Отправка уведомления | ADMIN |

### Subscriptions

| Method | Path | Описание | RBAC |
|--------|------|----------|------|
| `GET` | `/api/v1/notifications/subscriptions` | Список подписок | BUYER, SELLER |
| `POST` | `/api/v1/notifications/subscriptions` | Создать подписку | BUYER, SELLER |
| `DELETE` | `/api/v1/notifications/subscriptions/{id}` | Удалить подписку | BUYER, SELLER |

### Preferences

| Method | Path | Описание | RBAC |
|--------|------|----------|------|
| `GET` | `/api/v1/notifications/preferences` | Получить настройки | BUYER, SELLER |
| `PUT` | `/api/v1/notifications/preferences` | Обновить настройки | BUYER, SELLER |

### Messaging

| Method | Path | Описание | RBAC |
|--------|------|----------|------|
| `GET` | `/api/v1/messages/conversations` | Список диалогов | BUYER, SELLER |
| `GET` | `/api/v1/messages/conversations/{id}` | История диалога | BUYER, SELLER |
| `POST` | `/api/v1/messages/conversations` | Начать диалог | BUYER, SELLER |
| `GET` | `/api/v1/messages/conversations/{id}/messages` | Сообщения диалога | BUYER, SELLER |
| `POST` | `/api/v1/messages/conversations/{id}/messages` | Отправить сообщение | BUYER, SELLER |
| `PUT` | `/api/v1/messages/messages/{id}/read` | Пометить как прочитанное | BUYER, SELLER |

### Templates

| Method | Path | Описание | RBAC |
|--------|------|----------|------|
| `GET` | `/api/v1/messages/templates` | Список шаблонов | SELLER |
| `POST` | `/api/v1/messages/templates` | Создать шаблон | SELLER |
| `PUT` | `/api/v1/messages/templates/{id}` | Обновить шаблон | SELLER |
| `DELETE` | `/api/v1/messages/templates/{id}` | Удалить шаблон | SELLER |

### Attachments

| Method | Path | Описание | RBAC |
|--------|------|----------|------|
| `POST` | `/api/v1/messages/attachments` | Загрузить файл | BUYER, SELLER |
| `GET` | `/api/v1/messages/attachments/{id}` | Скачать файл | BUYER, SELLER |

### Video Calls

| Method | Path | Описание | RBAC |
|--------|------|----------|------|
| `POST` | `/api/v1/calls` | Создать видеозвонок | BUYER, SELLER |
| `GET` | `/api/v1/calls/{id}` | Получить видеозвонок | BUYER, SELLER |
| `POST` | `/api/v1/calls/{id}/join` | Присоединиться к звонку | BUYER, SELLER |
| `GET` | `/api/v1/calls/history` | История видеозвонков | BUYER, SELLER |

---

## Технические детали

### Конфигурация (application.yml)
```yaml
server:
  port: 8088

spring:
  application:
    name: communication-service
  datasource:
    driver-class-name: org.postgresql.Driver
    url: jdbc:postgresql://${COMM_DB_HOST:localhost}:${COMM_DB_PORT:5438}/${COMM_DB_NAME:services}
    username: ${COMM_DB_USER:postgres}
    password: ${COMM_DB_PASS:postgres}
  kafka:
    bootstrap-servers: ${KAFKA_BOOTSTRAP_SERVERS:localhost:9092}
  cloud:
    consul:
      host: ${CONSUL_HOST:localhost}
      port: ${CONSUL_PORT:8500}
  websocket:
    enabled: true
    path: /ws
  smtp:
    host: ${SMTP_HOST:localhost}
    port: ${SMTP_PORT:587}
    username: ${SMTP_USER}
    password: ${SMTP_PASS}
  sms:
    provider: ${SMS_PROVIDER:twilio}
    api-key: ${SMS_API_KEY}
  s3:
    bucket: ${S3_BUCKET:autodev-attachments}
```

### Зависимости (build.gradle.kts)
```kotlin
dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-websocket")
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

### Схема: `communication_service`

#### Таблица: `notifications`
```sql
CREATE TABLE communication_service.notifications (
    id                  BIGSERIAL      PRIMARY KEY,
    user_id             BIGINT         NOT NULL,
    type                VARCHAR(50)    NOT NULL,
    title               VARCHAR(255)   NOT NULL,
    body                TEXT           NOT NULL,
    is_read             BOOLEAN        NOT NULL   DEFAULT FALSE,
    created_at          TIMESTAMP      NOT NULL   DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES platform_service.users(id)
);
```

#### Таблица: `subscriptions`
```sql
CREATE TABLE communication_service.subscriptions (
    id                  BIGSERIAL      PRIMARY KEY,
    user_id             BIGINT         NOT NULL,
    event_type          VARCHAR(100)   NOT NULL,
    channel             VARCHAR(50)    NOT NULL,
    is_active           BOOLEAN        NOT NULL   DEFAULT TRUE,
    created_at          TIMESTAMP      NOT NULL   DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES platform_service.users(id)
);
```

#### Таблица: `conversations`
```sql
CREATE TABLE communication_service.conversations (
    id                  BIGSERIAL      PRIMARY KEY,
    product_id          BIGINT         NOT NULL,
    buyer_id            BIGINT         NOT NULL,
    seller_id           BIGINT         NOT NULL,
    last_message_at     TIMESTAMP      NOT NULL   DEFAULT CURRENT_TIMESTAMP,
    created_at          TIMESTAMP      NOT NULL   DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (product_id) REFERENCES catalog.products(id),
    FOREIGN KEY (buyer_id) REFERENCES platform_service.users(id),
    FOREIGN KEY (seller_id) REFERENCES platform_service.users(id)
);
```

#### Таблица: `messages`
```sql
CREATE TABLE communication_service.messages (
    id                  BIGSERIAL      PRIMARY KEY,
    conversation_id     BIGINT         NOT NULL,
    sender_id           BIGINT         NOT NULL,
    content             TEXT           NULL,
    attachment_id       BIGINT         NULL,
    is_read             BOOLEAN        NOT NULL   DEFAULT FALSE,
    created_at          TIMESTAMP      NOT NULL   DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (conversation_id) REFERENCES communication_service.conversations(id),
    FOREIGN KEY (sender_id) REFERENCES platform_service.users(id)
);
```

#### Таблица: `attachments`
```sql
CREATE TABLE communication_service.attachments (
    id                  BIGSERIAL      PRIMARY KEY,
    conversation_id     BIGINT         NOT NULL,
    sender_id           BIGINT         NOT NULL,
    file_name           VARCHAR(255)   NOT NULL,
    file_url            VARCHAR(500)   NOT NULL,
    file_size           BIGINT         NOT NULL,
    content_type        VARCHAR(100)   NOT NULL,
    created_at          TIMESTAMP      NOT NULL   DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (conversation_id) REFERENCES communication_service.conversations(id),
    FOREIGN KEY (sender_id) REFERENCES platform_service.users(id)
);
```

---

## Архитектурные решения

### WebSocket для чата
- **Использование:** Реальное время обмена сообщениями
- **Сервис:** Spring WebSocket
- **Аутентификация:** JWT в заголовке подключения

### Email/SMS/Push уведомления
- **Асинхронная отправка:** Через Kafka
- **Шаблоны:** Jinja2/Thymeleaf для HTML
- **Повторные попытки:** 3 раза с экспоненциальной задержкой

### Подписки
- **Event types:** order.updated, price.drop, new_review, new_message
- **Channels:** email, sms, push, in-app
- **Frequency:** instant, daily, weekly

### Видеозвонки
- **WebRTC:** Прямое соединение между пользователями
- ** signaling:** Согласование через our сервер
- **Запись:** Опциональная запись звонков

---

## Паттерны проектирования

### Observer Pattern
Использование Kafka для уведомления сервисов о событиях.

### Event Sourcing
События Kafka испо��ьзуются для асинхронной обработки сообщений и уведомлений.

---

## Межсервисное взаимодействие

### Синхронное (REST API)
- Communication Service → Platform Service (проверка пользователя)
- Communication Service → Catalog Service (получение товара для диалога)

### Асинхронное (Kafka)
- Communication Service → Kafka (события: notification.sent, message.created)
- Communication Service ← Kafka (события: order.completed, order.delivered)

---

## Безопасность

### Аутентификация
- JWT токены в заголовке `Authorization: Bearer {token}`
- Проверка токена через Auth Service
- WebSocket аутентификация через JWT

### Авторизация
- Пользователь может писать только в свои диалоги
- Продавец может писать покупателям о своих товарах
- ADMIN имеет полный доступ

### Шифрование
- TLS 1.3 для WebSocket
- AES-256 для файлов в S3
- Шифрование SMS в пути

---

## Метрики

| Метрика | Описание | Тип |
|---------|----------|-----|
| `notifications_sent_total` | Количество отправленных уведомлений | Counter |
| `messages_sent_total` | Количество отправленных сообщений | Counter |
| `conversations_created_total` | Количество созданных диалогов | Counter |
| `calls_total` | Количество видеозвонков | Counter |
| `email_deliveries_total` | Количество доставленных email | Counter |

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
  name: communication-service
spec:
  replicas: 2
  template:
    spec:
      containers:
      - name: communication-service
        image: autodev/communication-service:latest
        ports:
        - containerPort: 8088
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
- Отправка уведомлений
- Создание диалогов
- Обработка WebSocket сообщений

### Integration Tests
- Testcontainers для PostgreSQL, Kafka
- WireMock для мокирования SMTP
- Testcontainers для WebSocket

---

## Мониторинг

### Grafana Dashboard
- Communication Service Overview
- Notification Delivery
- Chat Activity
- Video Call Stats

---

## Риски и ограничения

### Текущие риски
1. **Email delivery failures** — письма могут попадать в спам
   - **Mitigation:** Проверка SPAM-статуса, альтернативные каналы

2. **WebSocket disconnects** — нестабильное соединение
   - **Mitigation:** Автоматическое восстановление, кэш непрочитанных сообщений

3. **File storage costs** — рост стоимости хранилища
   - **Mitigation:** Архивация старых файлов, сжатие

---

## План улучшений

### Short-term (1-2 недели)
- [ ] Реализовать WebSocket чат
- [ ] Интеграция с SendGrid/Smtp2Go для email
- [ ] Интеграция с Twilio для SMS

### Medium-term (1-2 месяца)
- [ ] Push уведомления через Firebase
- [ ] Шаблоны быстрых ответов
- [ ] Видеозвонки

### Long-term (3-6 месяцев)
- [ ] AI-ассистент для переписки
- [ ] Распознавание речи
- [ ] Перевод сообщений

---

## Контакты

- **Owner:** Backend Team
- **Slack:** #communication-service
- **Emergency:** #incident

---

## См. также

- [Консолидация сервисов](../service-consolidation.md) — детали консолидации
- [Системный обзор](../system-overview.md) — обзор архитектуры
- [OpenAPI спецификация](../api-specification/README.md) — API документация
- [ADR-0001](../architecture-decision-records/adr-0001-consolidation.md) — консолидация сервисов
