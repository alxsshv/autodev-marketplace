# AutoDev Marketplace — Политика взаимодействия сервисов

**Версия документа:** 1.0  
**Дата создания:** 2026-06-03  
**Последнее обновление:** 2026-06-03

---

## Обзор

Документ описывает правила и паттерны взаимодействия между микросервисами AutoDev Marketplace, включая синхронные вызовы, асинхронные сообщения, Service Discovery, Circuit Breaker и стратегии обработки ошибок.

---

## Общие принципы

### Синхронное взаимодействие
- **Использование:** Для прямых запросов с ожиданием ответа
- **Протокол:** HTTP/REST
- **Формат:** JSON
- **Библиотека:** Spring Cloud OpenFeign

### Асинхронное взаимодействие
- **Использование:** Для событийной архитектуры и слабой связанности
- **Протокол:** Kafka
- **Формат:** JSON (Avro для продакшена)
- **Библиотека:** Spring Kafka

### Service Discovery
- **Использование:** Динамическое обнаружение сервисов
- **Инструмент:** Consul
- **Health Check:** `/actuator/health`

---

## Синхронное взаимодействие (REST API)

### Когда использовать
| Сценарий | Метод |
|----------|-------|
| Прямой запрос с ожиданием ответа | REST API |
| Межсервисный вызов в рамках одного запроса | REST API |
| Запрос данных без side effects | REST API (GET) |

### Когда НЕ использовать
| Сценарий | Причина | Альтернатива |
|----------|---------|--------------|
| Уведомление о событии | Нет необходимости в ответе | Kafka |
| Асинхронная обработка | Тяжёлая операция | Kafka |
| Слабая связанность | Высокая доступность нужна | Kafka |

### REST API стандарты

#### Base URL
```
http://{service-name}.{namespace}.svc.cluster.local:8080
http://localhost:8080  # локально
```

#### Headers
| Header | Описание | Обязательный |
|--------|----------|-------------|
| `Authorization` | JWT токен | Да (для защищённых endpoints) |
| `X-Request-ID` | ID запроса для трассировки | Да |
| `X-Correlation-ID` | Correlation ID для логирования | Да |
| `Content-Type` | MIME type | Да (для POST/PUT/PATCH) |
| `Accept` | Ожидаемый формат ответа | Нет |

#### Ошибки API
```json
{
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Неверный формат email",
    "timestamp": "2026-06-03T10:30:00Z",
    "details": {
      "field": "email",
      "value": "invalid-email"
    }
  }
}
```

#### Коды HTTP
| Код | Описание |
|-----|----------|
| `200` | OK (GET, PUT, PATCH) |
| `201` | Created (POST) |
| `204` | No Content (DELETE) |
| `400` | Bad Request |
| `401` | Unauthorized |
| `403` | Forbidden |
| `404` | Not Found |
| `409` | Conflict |
| `429` | Too Many Requests |
| `500` | Internal Server Error |
| `503` | Service Unavailable |

### Пример использования (Feign Client)

```java
@FeignClient(
    name = "auth-service",
    url = "${auth.service.url:http://auth-service:8082}",
    configuration = FeignConfig.class
)
public interface AuthClient {
    
    @GetMapping("/api/v1/auth/me")
    UserDto getCurrentUser(@RequestHeader("Authorization") String token);
    
    @GetMapping("/api/v1/auth/users/{id}")
    UserDto getUserById(@RequestHeader("Authorization") String token, @PathVariable("id") Long id);
}
```

---

## Асинхронное взаимодействие (Kafka)

### Когда использовать
| Сценарий | Паттерн |
|----------|---------|
| Уведомление о событии | Event Publishing |
| Асинхронная обработка | Event Consumption |
| Слабая связанность | Event Collaboration |
| Event Sourcing | Event Sourcing |

### Kafka стандарты

#### Топики
```
<service>.<event-type>
```

**Примеры:**
- `auth.user_registered`
- `platform.user_profile_updated`
- `catalog.product_created`
- `order.order_created`
- `payment.payment_completed`

#### Структура события
```json
{
  "id": "uuid",
  "type": "user.profile_updated",
  "timestamp": "2026-06-03T10:30:00Z",
  "version": "1.0",
  "payload": {
    "user_id": 123,
    "email": "user@example.com",
    "first_name": "John",
    "last_name": "Doe"
  },
  "metadata": {
    "source_service": "user-service",
    "source_host": "user-service-1",
    "correlation_id": "abc-123"
  }
}
```

#### Key для сообщений
```
<entity-type>:<entity-id>
```

**Примеры:**
- `user:123`
- `order:456`
- `product:789`

### Пример использования

#### Publisher
```java
@Service
public class UserEventPublisher {
    
    private final KafkaTemplate<String, String> kafkaTemplate;
    
    public void publishUserRegistered(Long userId, String email) {
        String topic = "user.user_registered";
        String key = "user:" + userId;
        
        UserRegisteredEvent event = new UserRegisteredEvent(
            UUID.randomUUID().toString(),
            userId,
            email,
            LocalDateTime.now()
        );
        
        String payload = objectMapper.writeValueAsString(event);
        
        kafkaTemplate.send(topic, key, payload);
    }
}
```

#### Consumer
```java
@Service
public class UserRegisteredEventHandler {
    
    private final UserService userService;
    
    @KafkaListener(
        topics = "user.user_registered",
        groupId = "user-service-group"
    )
    public void handleUserRegistered(String payload) {
        UserRegisteredEvent event = objectMapper.readValue(payload, UserRegisteredEvent.class);
        
        userService.createUserProfile(event.getUserId(), event.getEmail());
    }
}
```

---

## Service Discovery (Consul)

### Регистрация сервиса
```yaml
spring:
  cloud:
    consul:
      host: ${CONSUL_HOST:localhost}
      port: ${CONSUL_PORT:8500}
      discovery:
        service-name: "user-service"
        health-check-path: "/actuator/health"
        health-check-interval: 15s
        health-check-timeout: 10s
        prefer-ip-address: true
        instance-id: ${spring.application.name}:${random.uuid}
```

### Обнаружение сервиса
```java
@Service
public class ServiceDiscoveryService {
    
    private final DiscoveryClient discoveryClient;
    
    public String getServiceUrl(String serviceName) {
        ServiceInstance instance = discoveryClient.getInstances(serviceName)
            .stream()
            .findFirst()
            .orElseThrow(() -> new ServiceNotFoundException(serviceName));
        
        return instance.getUri().toString();
    }
}
```

---

## Circuit Breaker (Resilience4j)

### Когда использовать
- Вызов внешних HTTP сервисов
- Вызов сервисов через Kafka (для dead letter handling)
- Любые операции с внешними зависимостями

### Настройки
```yaml
resilience4j:
  circuit-breaker:
    instances:
      auth-service:
        failure-rate-threshold: 50
        wait-duration-in-open-state: 5s
        ring-buffer-size-in-closed-state: 5
        ring-buffer-size-in-half-open-state: 3
        automatic-transition-from-open-to-half-open-enabled: true
      catalog-service:
        failure-rate-threshold: 50
        wait-duration-in-open-state: 10s
        ring-buffer-size-in-closed-state: 10
        ring-buffer-size-in-half-open-state: 5
```

### Пример использования
```java
@Service
public class AuthClientService {
    
    private final CircuitBreaker circuitBreaker;
    
    public AuthClientService() {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
            .failureRateThreshold(50)
            .waitDurationInOpenState(Duration.ofSeconds(5))
            .ringBufferSizeInClosedState(5)
            .ringBufferSizeInHalfOpenState(3)
            .build();
        
        circuitBreaker = CircuitBreaker.of("auth-service", config);
    }
    
    @CircuitBreaker(name = "auth-service", fallbackMethod = "fallback")
    public UserDto getCurrentUser(String token) {
        return authClient.getCurrentUser(token);
    }
    
    private UserDto fallback(String token, Throwable throwable) {
        log.error("Auth service is down", throwable);
        return new UserDto(null, "Service unavailable", null, null);
    }
}
```

---

## Retry Pattern

### Когда использовать
- Временные сбои (network issues, service restart)
- Kafka message delivery
- HTTP call timeouts

### Когда НЕ использовать
- Дублирующиеся операции (оплата, создание заказа)
- Операции без idempotency

### Настройки
```yaml
spring:
  retry:
    max-attempts: 3
    initial-interval: 1000
    multiplier: 2.0
    max-interval: 10000
```

### Пример использования
```java
@Service
public class UserService {
    
    @Retryable(
        value = {RemoteAccessException.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2.0)
    )
    public UserDto createUser(UserDto userDto) {
        return restTemplate.postForObject(
            "http://auth-service/api/v1/auth/users",
            userDto,
            UserDto.class
        );
    }
    
    @Recover
    public UserDto recover(RemoteAccessException e, UserDto userDto) {
        log.error("Failed to create user after 3 attempts", e);
        throw new UserCreationException("User creation failed", e);
    }
}
```

---

## Idempotency

### Когда требуется
- Оплата
- Создание заказа
- Изменение статуса заказа

### Реализация

#### Идемпотентный ключ
```java
@RestController
public class OrderController {
    
    @PostMapping("/api/v1/orders")
    public ResponseEntity<OrderDto> createOrder(
        @RequestBody CreateOrderRequest request,
        @RequestHeader("X-Idempotency-Key") String idempotencyKey
    ) {
        // Проверка существующего заказа с этим ключом
        Order existingOrder = orderService.findByIdempotencyKey(idempotencyKey);
        if (existingOrder != null) {
            return ResponseEntity.ok(mapToDto(existingOrder));
        }
        
        // Создание нового заказа
        Order order = orderService.createOrder(request);
        orderService.saveIdempotencyKey(idempotencyKey, order.getId());
        
        return ResponseEntity.status(HttpStatus.CREATED).body(mapToDto(order));
    }
}
```

#### Redis для хранения
```redis
KEY: order:idempotency:{key}
VALUE: order_id
TTL: 1 hour
```

---

## Стратегия обработки ошибок

### Сетевые ошибки
| Код | Действие |
|-----|----------|
| `400` | Показать ошибку клиенту |
| `401` | Перенаправить на login |
| `403` | Показать ошибку доступа |
| `404` | Показать ошибку не найдено |
| `429` | Показать ошибку rate limit |
| `500` | Логировать, показать generic error |
| `503` | Использовать Circuit Breaker, Retry |

### Kafka ошибки
| Сценарий | Действие |
|----------|----------|
| Message deserialization error | Send to DLQ |
| Business validation error | Log and acknowledge |
| Transient error | Retry with exponential backoff |
| Persistent error | Send to DLQ after max retries |

---

## Transactional Outbox Pattern

### Когда использовать
- Гарантированная доставка событий Kafka при записи в БД

### Реализация
```sql
CREATE TABLE order_service.outbox (
    id                  BIGSERIAL      PRIMARY KEY,
    aggregate_type      VARCHAR(255)   NOT NULL,
    aggregate_id        VARCHAR(255)   NOT NULL,
    event_type          VARCHAR(255)   NOT NULL,
    payload             JSONB          NOT NULL,
    created_at          TIMESTAMP      NOT NULL   DEFAULT CURRENT_TIMESTAMP,
    published           BOOLEAN        NOT NULL   DEFAULT FALSE
);
```

```java
@Service
public class OrderService {
    
    @Transactional
    public Order createOrder(CreateOrderRequest request) {
        Order order = new Order(...);
        orderRepository.save(order);
        
        outboxRepository.save(new Outbox(
            "order",
            order.getId().toString(),
            "order.created",
            objectMapper.writeValueAsString(order)
        ));
        
        return order;
    }
}
```

---

## Saga Pattern

### Когда использовать
- Распределённые транзакции (оформление заказа)

### Реализация (compensation-based)
```mermaid
sequenceDiagram
    participant OrderService
    participant InventoryService
    participant PaymentService
    participant NotificationService

    OrderService->>OrderService: Begin transaction
    OrderService->>InventoryService: Reserve stock
    InventoryService-->>OrderService: Stock reserved
    OrderService->>PaymentService: Process payment
    PaymentService-->>OrderService: Payment processed
    OrderService->>NotificationService: Send notification
    NotificationService-->>OrderService: Notification sent
    OrderService->>OrderService: Commit transaction
    
    Note over OrderService,NotificationService: Если ошибка в любом шаге,<br/>выполнить компенсацию (отменить резервирование)
```

---

## Health Check & Readiness

### Health Check
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health
  endpoint:
    health:
      show-details: when_authorized
      probes:
        enabled: true
```

### Readiness Probe
- База данных доступна
- Redis доступен
- Kafka доступен
- Consul зарегистрировал сервис

---

## Monitoring & Observability

### Metriки
| Метрика | Описание |
|---------|----------|
| `http_client_requests_seconds_count` | Количество HTTP запросов |
| `http_client_requests_seconds_sum` | Суммарное время |
| `kafka_producer_records_success_total` | Успешные сообщения |
| `kafka_consumer_records_consumed_total` | Прочитанные сообщения |
| `circuit_breaker_state` | Состояние Circuit Breaker |

### Трассировка
- Корреляционный ID передаётся через заголовки
- Логирование всех вызовов
- Tracing через Tempo

---

## Заключение

Политика взаимодействия сервисов:
- **REST API** для прямых вызовов с ожиданием ответа
- **Kafka** для событийной архитектуры
- **Consul** для Service Discovery
- **Resilience4j** для отказоустойчивости
- **Retry Pattern** для временных сбоев
- **Idempotency** для критичных операций
- **Transactional Outbox** для надёжной доставки событий
- **Saga Pattern** для распределённых транзакций
