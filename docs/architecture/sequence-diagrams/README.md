# Sequence Diagrams for AutoDev Marketplace MVP

**Версия документа:** 1.0  
**Дата создания:** 2026-06-04  
**Последнее обновление:** 2026-06-04  

---

## Обзор

В этом каталоге содержатся диаграммы последовательностей (Sequence Diagrams) для ключевых бизнес-сценариев AutoDev Marketplace MVP.

Сервисы (8 для MVP):
1. **api-gateway** - единая точка входа
2. **auth-service** - аутентификация и авторизация
3. **catalog-service** - каталог, цены, наличие
4. **order-service** - заказы, доставка, возвраты
5. **search-service** - поиск
6. **payment-service** - оплата
7. **communication-service** - уведомления и чат
8. **platform-service** - пользователи, модерация, отзывы

---

## Сценарии

### 1. Регистрация пользователя и создание профиля

```
sequenceDiagram
    participant Client
    participant APIGateway
    participant Keycloak
    participant AuthService
    participant PlatformService
    participant Kafka

    Note over Client,PlatformService: Регистрация через Keycloak Admin API или Console
    Client->>Keycloak: POST /admin/realms/{realm}/users (через Admin API)
    Keycloak-->>Client: 201 Created (keycloak_user_id)
    Keycloak->>AuthService: Webhook/Event о новом пользователе
    AuthService->>AuthService: Создание записи в auth.users (синхронизация)
    AuthService->>Kafka: auth.user_registered
    Kafka-->>PlatformService: auth.user_registered (событие)
    PlatformService->>PlatformService: Создание профиля пользователя
    PlatformService-->>Client: 201 Created (profilе created)
```

### 2. Оформление заказа

```
sequenceDiagram
    participant Client
    participant APIGateway
    participant OrderService
    participant CatalogService
    participant PaymentService
    participant CommunicationService
    participant Kafka

    Client->>APIGateway: POST /api/v1/orders
    APIGateway->>OrderService: POST /api/v1/orders
    
    OrderService->>CatalogService: GET /api/v1/catalog/products/{id}
    CatalogService-->>OrderService: Product data
    
    OrderService->>Kafka: order.order.created
    Note right of OrderService: Saga start
    
    OrderService->>CatalogService: PUT /api/v1/catalog/products/{id}/reserve
    CatalogService-->>OrderService: Reserved
    CatalogService->>Kafka: catalog.product.reserved
    
    OrderService->>PaymentService: POST /api/v1/payments/initiate
    PaymentService->>Kafka: payment.payment_initiated
    
    PaymentService-->>OrderService: Payment initiated
    OrderService->>Kafka: order.payment_initiated
    
    OrderService->>CommunicationService: POST /api/v1/notifications
    CommunicationService-->>Client: Email notification
```

### 3. Поиск товаров

```
sequenceDiagram
    participant Client
    participant APIGateway
    participant SearchService
    participant Elasticsearch

    Client->>APIGateway: GET /api/v1/search?query=Toyota
    APIGateway->>SearchService: GET /api/v1/search
    
    SearchService->>Elasticsearch: search products
    Elasticsearch-->>SearchService: Results
    
    SearchService-->>APIGateway: Search results
    APIGateway-->>Client: 200 OK
```

### 4. Оплата заказа и эскроу

```
sequenceDiagram
    participant Client
    participant APIGateway
    participant OrderService
    participant PaymentService
    participant Sberbank
    participant Kafka

    Client->>APIGateway: POST /api/v1/payments/confirm
    APIGateway->>OrderService: GET /api/v1/orders/{id}
    OrderService-->>APIGateway: Order details
    APIGateway-->>Client: Order info
    
    Client->>APIGateway: POST /api/v1/payments/confirm
    APIGateway->>PaymentService: POST /api/v1/payments/initiate
    PaymentService->>Sberbank: Initiate payment
    Sberbank-->>PaymentService: 3DS challenge
    PaymentService-->>Client: 3DS redirect
    
    Client->>Sberbank: Complete 3DS
    Sberbank-->>PaymentService: Webhook: payment.confirmed
    PaymentService->>Kafka: payment.payment_confirmed
    PaymentService->>Kafka: escrow.funds_locked
    
    PaymentService-->>APIGateway: Payment confirmed
    APIGateway-->>Client: 200 OK
```

### 5. Модерация контента

```
sequenceDiagram
    participant Seller
    participant APIGateway
    participant PlatformService
    participant Kafka

    Seller->>APIGateway: POST /api/v1/platform/moderation/items
    APIGateway->>PlatformService: POST /api/v1/platform/moderation/items
    PlatformService->>Kafka: platform.moderation.created
    
    PlatformService-->>Seller: 201 Created
    Note right of PlatformService: Item pending moderation
    
    moderator(Admin/Mod)++PlatformService: Review item
    
    PlatformService->>Kafka: platform.moderation.approved
    
    PlatformService-->>Seller: 200 OK
    Note right of PlatformService: Item published
```

### 6. Отправка уведомления

```
sequenceDiagram
    participant OrderService
    participant Kafka
    participant CommunicationService
    participant SMTP
    participant PushProvider

    OrderService->>Kafka: order.order.completed
    Kafka-->>CommunicationService: order.order.completed
    
    CommunicationService->>CommunicationService: Build email template
    
    CommunicationService->>SMTP: Send email
    SMTP-->>CommunicationService: 200 OK
    
    CommunicationService->>PushProvider: Send push
    PushProvider-->>CommunicationService: 200 OK
    
    CommunicationService->>Kafka: communication.notification.sent
```

---

## Детали сценариев

### Регистрация пользователя
1. Пользователь отправляет данные на Keycloak (через Admin API или Console)
2. Keycloak создает пользователя и возвращает keycloak_user_id
3. Keycloak отправляет webhook/event в Auth Service о новом пользователе
4. Auth Service создает запись в PostgreSQL (синхронизация)
5. Auth Service отправляет событие auth.user_registered в Kafka
6. Platform Service слушает событие и создает профиль пользователя

### Оформление заказа
1. Client отправляет запрос на оформление заказа
2. Order Service проверяет наличие товара (Catalog Service)
3. Order Service создаёт событие заказа в Kafka
4. Catalog Service резервирует товар
5. Order Service инициирует платеж (Payment Service)
6. Payment Service использует эскроу-счёт

### Поиск товаров
1. Client отправляет запрос на поиск
2. API Gateway направляет запрос в Search Service
3. Search Service делает запрос в Elasticsearch
4. Elasticsearch возвращает результаты
5. Search Service возвращает результаты клиенту

### Оплата заказа
1. Order Service запрашивает информацию о заказе
2. Payment Service инициирует платёж через Сбербанк
3. Сбербанк требует 3DS challenge
4. После подтверждения платежа, Payment Service блокирует средства на эскроу-счёте
5. Event sent to Kafka for other services

### Модерация контента
1. Seller отправляет контент на модерацию
2. Platform Service сохраняет и отправляет событие в Kafka
3. Администратор или модератор проверяет контент
4. После одобрения, контент публикуется

### Отправка уведомления
1. Order Service отправляет событие завершения заказа
2. Communication Service слушает событие и отправляет email и push
3. Подтверждение отправки отправляется обратно в Kafka

---

## Легенда

- **Синий прямоугольник:** Клиент
- **Синий прямоугольник с обводкой:** API Gateway
- **Зелёный прямоугольник:** Сервис
- **Фиолетовый прямоугольник:** Внешний сервис (Database, Kafka, SMTP)
- **Красная стрелка:** Входящий запрос
- **Зелёная стрелка:** Исходящий запрос
- **Серая пунктирная стрелка:** Событие в Kafka

---

## Инструменты

- **Mermaid** - редактирование диаграмм
- **VS Code + Mermaid Preview** - визуализация

---

## Поддержка

Для вопросов по диаграммам последовательностей обращайтесь к команде архитектуры.
