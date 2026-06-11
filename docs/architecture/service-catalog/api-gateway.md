# API Gateway Service

**Версия документа:** 1.0  
**Дата создания:** 2026-06-03  
**Последнее обновление:** 2026-06-03

---

## Обзор

API Gateway — это единая точка входа для всех клиентов (web SPA, мобильное приложение, админка). Выполняет маршрутизацию запросов к соответствующим микросервисам, аутентификацию через JWT, ограничение частоты запросов (rate limiting) и управление политиками CORS.

---

## Бизнес-функция

API Gateway — это транспортный слой, который:
- Принимает HTTP/HTTPS запросы от клиентов
- Маршрутизирует запросы к соответствующим микросервисам
- Проверяет JWT токены аутентификации
- Ограничивает частоту запросов (rate limiting)
- Обрабатывает CORS
- Логирует и трассирует запросы
- Возвращает единообразные ответы с ошибками

---

## API endpoints

### Проксируемые endpoints

| Method | Path | Сервис | Описание |
|--------|------|--------|----------|
| `GET` | `/api/v1/auth/**` | auth-service | Аутентификация и авторизация |
| `GET` | `/api/v1/users/**` | platform-service | Управление пользователями |
| `GET` | `/api/v1/catalog/**` | catalog-service | Каталог товаров |
| `GET` | `/api/v1/orders/**` | order-service | Управление заказами |
| `GET` | `/api/v1/search/**` | search-service | Поиск товаров |
| `GET` | `/api/v1/payments/**` | payment-service | Платежи |
| `GET` | `/api/v1/notifications/**` | communication-service | Уведомления и чат |

### Actuator endpoints

| Method | Path | Описание |
|--------|------|----------|
| `GET` | `/actuator/health` | Состояние сервиса |
| `GET` | `/actuator/info` | Информация о сервисе |
| `GET` | `/actuator/prometheus` | Метрики для Prometheus |

---

## Технические детали

### Конфигурация (application.yml)
```yaml
server:
  port: 8080

spring:
  application:
    name: api-gateway
  cloud:
    gateway:
      discovery:
        locator:
          enabled: false
      routes:
        - id: auth-service
          uri: lb://auth-service
          predicates:
            - Path=/api/v1/auth/**
          filters:
            - RewritePath=/api/v1/(?<path>.*), /$\{path}
        # ... другие маршруты
    consul:
      host: ${CONSUL_HOST:localhost}
      port: ${CONSUL_PORT:8500}
      discovery:
        service-name: "api-gateway"
        health-check-path: "/actuator/health"
```

### Зависимости (build.gradle.kts)
```kotlin
dependencies {
    implementation("org.springframework.cloud:spring-cloud-starter-gateway")
    implementation("org.springframework.cloud:spring-cloud-starter-consul-discovery")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("io.micrometer:micrometer-registry-prometheus")
}
```

---

## Архитектурные решения

### Выбор Spring Cloud Gateway
**Преимущества:**
- Совместимость с Spring Boot 3.x
- Поддержка WebFlux (реактивный подход)
- Встроенная поддержка rate limiting, circuit breaker
- Интеграция с Consul

**Альтернативы:**
- Nginx — более сложная конфигурация, нет Java-экосистемы
- Kong — требует дополнительной инфраструктуры
- Tyk — менее популярен

### Стратегия маршрутизации
- **Discovery Client** — автоматическое обнаружение сервисов через Consul
- **Path-based routing** — маршрутизация по префиксу пути (`/api/v1/auth/**`)
- **RewritePath filter** — удаление префикса `/api/v1` перед отправкой в сервис

### Rate Limiting
- **Используется:** Redis rate limiter
- **Конфигурация:** 1000 запросов в минуту на IP
- **Возврат:** 429 Too Many Requests

### Circuit Breaker
- **Библиотека:** Resilience4j
- **Настройки:**
  - failureRateThreshold: 50%
  - waitDurationInOpenState: 5s
  - ringBufferSizeInHalfOpenState: 5
- **Fallback:** Возврат кэшированных данных или сообщения об ошибке

---

## Межсервисное взаимодействие

### Синхронное (REST API)
- API Gateway → Microservices (через Consul Discovery)
- Используется LoadBalancerClient для балансировки

### Логирование и трассировка
- **Logging Filter** — запись входящих запросов
- **Tracing Filter** — передача correlation ID в заголовках
- **Micrometer** — сбор метрик

---

## Интеграции

### Keycloak Integration
- Проверка JWT токенов в заголовке `Authorization`
- Валидация токенов через Keycloak endpoint `/realms/autodev/protocol/openid-connect/userinfo`
- Extract roles и permissions из токена

### Consul Integration
- Регистрация сервиса `api-gateway`
- Health check: `/actuator/health`
- Дискавери других сервисов через `/v1/agent/services`

---

## Метрики

| Метрика | Описание | Тип |
|---------|----------|-----|
| `http_server_requests_seconds_count` | Количество HTTP запросов | Counter |
| `http_server_requests_seconds_sum` | Суммарное время обработки | Summary |
| `gateway_routes_total` | Количество маршрутов | Gauge |
| `gateway_route_errors_total` | Количество ошибок маршрутизации | Counter |
| `rate_limiter_errors_total` | Количество превышений rate limit | Counter |

---

## SLA/SLO

| Метрика | Целевое значение | Измерение |
|---------|-----------------|-----------|
| Доступность | 99.9% | Uptime (Prometheus) |
| Latency (p95) | <100 мс | Tempo traces |
| Latency (p99) | <500 мс | Tempo traces |
| Ошибки (p99) | <1% | Prometheus errors |

---

## Deployment

### Kubernetes
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: api-gateway
spec:
  replicas: 3
  template:
    spec:
      containers:
      - name: api-gateway
        image: autodev/api-gateway:latest
        ports:
        - containerPort: 8080
        resources:
          requests:
            memory: "256Mi"
            cpu: "250m"
          limits:
            memory: "512Mi"
            cpu: "500m"
```

### Docker Compose
```yaml
api-gateway:
  build: ./services/api-gateway
  container_name: api-gateway
  ports:
    - "8081:8080"
  environment:
    - SPRING_PROFILES_ACTIVE=docker
  depends_on:
    keycloak:
      condition: service_started
  healthcheck:
    test: ["CMD", "wget", "--spider", "http://localhost:8080/actuator/health"]
    interval: 10s
    timeout: 5s
    retries: 20
    start_period: 40s
```

---

## Тестирование

### Unit Tests
- Маршрутизация запросов
- Валидация JWT токенов
- Rate limiting

### Integration Tests
- WireMock для мокирования микросервисов
- Тестирование полного цепочки: Client → Gateway → Service

### Load Tests
- JMeter: 1000 RPS
- Проверка rate limiting

---

## Мониторинг

### Grafana Dashboard
- API Gateway Overview
- Request Latency
- Error Rate
- Circuit Breaker Status
- Rate Limiter Stats

### Алерты
- `HighErrorRate`: >5% ошибок за 5 минут
- `HighLatency`: p95 >500ms за 10 минут
- `ServiceDown`: Сервис недоступен

---

## Риски и ограничения

### Текущие риски
1. **Single point of failure** — если Gateway упадёт, все клиенты не смогут подключиться
   - **Mitigation:** 3 реплики с Kubernetes HPA

2. **Latency overhead** — каждый запрос проходит через несколько фильтров
   - **Mitigation:** Кэширование маршрутов, асинхронная обработка

3. **JWT validation bottleneck** — валидация токенов на каждый запрос
   - **Mitigation:** Кэширование валидных токенов в Redis

---

## План улучшений

### Short-term (1-2 недели)
- [ ] Реализовать кэширование JWT валидации
- [ ] Настроить кастомные ошибки API
- [ ] Добавить circuit breaker для всех сервисов

### Medium-term (1-2 месяца)
- [ ] GraphQL API для гибкой загрузки данных
- [ ] WebSocket support для реального времени
- [ ] Mutual TLS для межсервисной коммуникации

### Long-term (3-6 месяцев)
- [ ] Service Mesh (Istio) для продвинутой маршрутизации
- [ ]canary deployments через Envoy
- [ ] API versioning через заголовки

---

## Контакты

- **Owner:** Backend Team
- **Slack:** #api-gateway
- **Emergency:** #incident

---

## См. также

- [Консолидация сервисов](../service-consolidation.md) — детали консолидации
- [Системный обзор](../system-overview.md) — обзор архитектуры
- [Диаграмма компонентов](../component-diagram.md) — диаграмма компонентов
- [OpenAPI спецификация](../api-specification/README.md) — API документация
- [ADR-0001](../architecture-decision-records/adr-0001-consolidation.md) — консолидация сервисов
