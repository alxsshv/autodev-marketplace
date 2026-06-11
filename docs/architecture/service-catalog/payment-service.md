# Payment Service

**Версия документа:** 1.0  
**Дата создания:** 2026-06-04  
**Последнее обновление:** 2026-06-04

---

## Обзор

Payment Service — это сервис обработки платежей и обеспечения безопасных сделок. Он реализует эскроу-счёт для защиты покупателя и продавца, интегрируется с платёжными системами (Сбербанк, Тинькофф), и управляет способами оплаты и историей платежей.

---

## Бизнес-функция

Payment Service обеспечивает:
- Обработку оплаты картой через 3D-Secure
- Безопасную сделку (эскроу-счёт)
- Хранение и управление способами оплаты
- Историю всех платежей
- Интеграцию с внешними платёжными системами
- Возвраты платежей (refunds)
- Проверку платежей (webhook verification)

---

## API endpoints

### Payment Processing

| Method | Path | Описание | RBAC |
|--------|------|----------|------|
| `POST` | `/api/v1/payments/initiate` | Инициализация оплаты | BUYER |
| `POST` | `/api/v1/payments/confirm` | Подтверждение оплаты | BUYER |
| `POST` | `/api/v1/payments/refund` | Возврат средств | SELLER, ADMIN |

### Escrow Management

| Method | Path | Описание | RBAC |
|--------|------|----------|------|
| `GET` | `/api/v1/payments/escrow/{id}` | Получение эскроу-счёта | BUYER, SELLER |
| `POST` | `/api/v1/payments/escrow/release` | Освобождение средств продавцу | BUYER, ADMIN |
| `POST` | `/api/v1/payments/escrow/refund` | Возврат средств покупателю | SELLER, ADMIN |

### Payment Methods

| Method | Path | Описание | RBAC |
|--------|------|----------|------|
| `GET` | `/api/v1/payments/methods` | Список способов оплаты | BUYER |
| `POST` | `/api/v1/payments/methods` | Добавление способа оплаты | BUYER |
| `DELETE` | `/api/v1/payments/methods/{id}` | Удаление способа оплаты | BUYER |
| `PUT` | `/api/v1/payments/methods/{id}` | Установка по умолчанию | BUYER |

### Payment History

| Method | Path | Описание | RBAC |
|--------|------|----------|------|
| `GET` | `/api/v1/payments/history` | История платежей | BUYER, SELLER |
| `GET` | `/api/v1/payments/history/{id}` | Получение платежа по ID | BUYER, SELLER |
| `GET` | `/api/v1/payments/invoices` | Счета и чеки | BUYER, SELLER |

---

## Технические детали

### Конфигурация (application.yml)
```yaml
server:
  port: 8087

spring:
  application:
    name: payment-service
  datasource:
    driver-class-name: org.postgresql.Driver
    url: jdbc:postgresql://${PAYMENT_DB_HOST:localhost}:${PAYMENT_DB_PORT:5438}/${PAYMENT_DB_NAME:services}
    username: ${PAYMENT_DB_USER:postgres}
    password: ${PAYMENT_DB_PASS:postgres}
  kafka:
    bootstrap-servers: ${KAFKA_BOOTSTRAP_SERVERS:localhost:9092}
  cloud:
    consul:
      host: ${CONSUL_HOST:localhost}
      port: ${CONSUL_PORT:8500}
  integration:
    sberbank:
      api-url: ${SBER_API_URL}
      login: ${SBER_LOGIN}
      password: ${SBER_PASSWORD}
    tinkoff:
      api-url: ${TINKOFF_API_URL}
      token: ${TINKOFF_TOKEN}
```

### Зависимости (build.gradle.kts)
```kotlin
dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.cloud:spring-cloud-starter-consul-discovery")
    implementation("org.springframework.cloud:spring-cloud-starter-consul-config")
    implementation("org.springframework.kafka:spring-kafka")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    
    implementation("org.postgresql:postgresql")
    implementation("org.liquibase:liquibase-core")
    
    implementation("io.micrometer:micrometer-registry-prometheus")
}
```

---

## Модель данных

### Схема: `payment_service`

#### Таблица: `payments`
```sql
CREATE TABLE payment_service.payments (
    id                  BIGSERIAL      PRIMARY KEY,
    order_id            BIGINT         NOT NULL UNIQUE,
    amount              NUMERIC(10,2)  NOT NULL,
    currency            VARCHAR(3)     NOT NULL   DEFAULT 'RUB',
    payment_method      VARCHAR(50)    NOT NULL,
    status              VARCHAR(50)    NOT NULL,
    escrow_account_id   VARCHAR(255)   NULL,
    external_payment_id VARCHAR(255)   NULL,
    payment_details     JSONB          NULL,
    created_at          TIMESTAMP      NOT NULL   DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP      NOT NULL   DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES order_service.orders(id)
);
```

#### Таблица: `escrow_accounts`
```sql
CREATE TABLE payment_service.escrow_accounts (
    id                  BIGSERIAL      PRIMARY KEY,
    order_id            BIGINT         NOT NULL UNIQUE,
    buyer_id            BIGINT         NOT NULL,
    seller_id           BIGINT         NOT NULL,
    amount              NUMERIC(10,2)  NOT NULL,
    status              VARCHAR(50)    NOT NULL,
    released_at         TIMESTAMP      NULL,
    refunded_at         TIMESTAMP      NULL,
    created_at          TIMESTAMP      NOT NULL   DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (buyer_id) REFERENCES platform_service.users(id),
    FOREIGN KEY (seller_id) REFERENCES platform_service.users(id)
);
```

#### Таблица: `payment_methods`
```sql
CREATE TABLE payment_service.payment_methods (
    id                  BIGSERIAL      PRIMARY KEY,
    user_id             BIGINT         NOT NULL,
    provider            VARCHAR(50)    NOT NULL,
    provider_method_id  VARCHAR(255)   NOT NULL,
    card_type           VARCHAR(50)    NULL,
    card_last_four      VARCHAR(4)     NULL,
    is_default          BOOLEAN        NOT NULL   DEFAULT FALSE,
    created_at          TIMESTAMP      NOT NULL   DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES platform_service.users(id)
);
```

#### Таблица: `payment_history`
```sql
CREATE TABLE payment_service.payment_history (
    id                  BIGSERIAL      PRIMARY KEY,
    payment_id          BIGINT         NOT NULL,
    event_type          VARCHAR(50)    NOT NULL,
    amount              NUMERIC(10,2)  NOT NULL,
    details             JSONB          NULL,
    created_at          TIMESTAMP      NOT NULL   DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (payment_id) REFERENCES payment_service.payments(id)
);
```

---

## Архитектурные решения

### Escrow Payment System
- **При оплате:** Средства блокируются на эскроу-счёте
- **При доставке:** Средства переводятся продавцу
- **При возврате:** Средства возвращаются покупателю

### 3D-Secure Integration
- Поддержка 3DS2 для всех карт
- Challenge flow для подтверждения оплаты
- SecureCode/VAS для российских карт

### Интеграция с платёжными системами
**Сбербанк:**
- API для создания платежа
- Webhook для уведомлений о статусе
- Проверка статуса платежа

**Тинькофф:**
- API для создания платежа
- Webhook для уведомлений
- Проверка статуса платежа

### События Kafka
- `payment.payment_initiated` — инициализация платежа
- `payment.payment_confirmed` — подтверждение платежа
- `payment.payment_failed` — неудачная оплата
- `payment.refund_processed` — возврат средств
- `escrow.funds_released` — освобождение эскроу-счёта
- `escrow.funds_refunded` — возврат эскроу-счёта

---

## Паттерны проектирования

### Saga Pattern
Координация оплаты и доставки: заказ → оплата → доставка → завершение

### Event Sourcing
События Kafka используются для асинхронной обработки платежей и возвратов.

---

## Межсервисное взаимодействие

### Синхронное (REST API)
- Payment Service → Order Service (проверка заказа)
- Payment Service → Platform Service (проверка пользователя)
- Payment Service → Sberbank/Tinkoff API (обработка платежа)

### Асинхронное (Kafka)
- Payment Service → Kafka (события платежа)
- Payment Service ← Kafka (события: order.delivered, order.cancelled)

---

## Безопасность

### Аутентификация
- JWT токены в заголовке `Authorization: Bearer {token}`
- Проверка токена через Auth Service

### Авторизация
- Пользователь может управлять только своими платежами
- Продавец может видеть платежи по своим товарам
- ADMIN имеет полный доступ

### Шифрование
- PCI-DSS compliance для хранения данных карт
- AES-256 шифрование чувствительных данных в БД
- TLS 1.3 для всех внешних вызовов

---

## Метрики

| Метрика | Описание | Тип |
|---------|----------|-----|
| `payment_initiated_total` | Количество инициированных платежей | Counter |
| `payment_confirmed_total` | Количество подтверждённых платежей | Counter |
| `payment_failed_total` | Количество неудачных платежей | Counter |
| `refund_processed_total` | Количество возвратов | Counter |
| `escrow_operations_total` | Количество операций с эскроу | Counter |

---

## SLA/SLO

| Метрика | Целевое значение | Измерение |
|---------|-----------------|-----------|
| Доступность | 99.9% | Uptime (Prometheus) |
| Latency (p95) | <500 мс | Tempo traces |
| Latency (p99) | <1500 мс | Tempo traces |
| Ошибки (p99) | <1% | Prometheus errors |

---

## Deployment

### Kubernetes
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: payment-service
spec:
  replicas: 2
  template:
    spec:
      containers:
      - name: payment-service
        image: autodev/payment-service:latest
        ports:
        - containerPort: 8087
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
- Инициализация платежа
- Подтверждение платежа
- Возврат средств

### Integration Tests
- Testcontainers для PostgreSQL, Kafka
- WireMock для мокирования платёжных систем
- Симуляция webhook-ов

---

## Мониторинг

### Grafana Dashboard
- Payment Service Overview
- Payment Lifecycle
- Escrow Management
- Refund Statistics

### Алерты
- `HighPaymentFailureRate`: >5% неудачных платежей за 5 минут
- `EscrowLockTimeout`: Эскроу-счёт заблокирован более 30 минут
- `ServiceDown`: Сервис недоступен

---

## Риски и ограничения

### Текущие риски
1. **Payment failures** — неудачи из-за проблем с банком
   - **Mitigation:** Повторные попытки, уведомления пользователя

2. **Escrow lock timeout** — блокировка средств может превысить лимит
   - **Mitigation:** Автоматическое освобождение после таймаута

3. **Webhook reliability** — уведомления от банков могут теряться
   - **Mitigation:** Повторные попытки и ручная проверка

---

## План улучшений

### Short-term (1-2 недели)
- [ ] Реализовать полную интеграцию с Сбербанком
- [ ] Реализовать интеграцию с Тинькофф
- [ ] Добавить webhook-обработку

### Medium-term (1-2 месяца)
- [ ] Поддержка QIWI, Яндекс.Деньги
- [ ] Крипто-платежи (Bitcoin, USDT)
- [ ] Рассрочка и кредитование

### Long-term (3-6 месяцев)
- [ ] Платежи в криптовалюте
- [ ] Микроплатежи
- [ ] Платежные консультанты (AI)

---

## Контакты

- **Owner:** Backend Team
- **Slack:** #payment-service
- **Emergency:** #incident

---

## См. также

- [Консолидация сервисов](../service-consolidation.md) — детали консолидации
- [Системный обзор](../system-overview.md) — обзор архитектуры
- [OpenAPI спецификация](../api-specification/README.md) — API документация
- [ADR-0001](../architecture-decision-records/adr-0001-consolidation.md) — консолидация сервисов
