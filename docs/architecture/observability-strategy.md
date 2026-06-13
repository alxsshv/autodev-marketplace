# AutoDev Marketplace — Стратегия мониторинга и наблюдаемости

**Версия документа:** 1.1  
**Дата создания:** 2026-06-03  
**Последнее обновление:** 2026-06-13

---

## 1. Метрики безопасности

### 1.1 Аутентификация и авторизация
| Метрика | Описание | Целевое значение |
|---------|----------|-----------------|
| `auth_login_attempts_total` | Попытки входа | Counter |
| `auth_login_failures_total` | Неудачные попытки | < 10/5min |
| `auth_token_validations_total` | Валидации токенов | Counter |
| `auth_token_cache_hits_total` | Попадания в кэш | > 90% |
| `auth_service_token_requests_total` | Запросы service tokens | Counter |

### 1.2 Входящие запросы (HTTP)
| Метрика | Описание | Целевое значение |
|---------|----------|-----------------|
| `http_server_requests_seconds_count` | Количество запросов | Counter |
| `http_server_requests_seconds_sum` | Суммарное время | Summary |
| `http_server_requests_seconds_max` | Максимальное время | Gauge |
| `http_server_requests_total{status="401"}` | Unauthorized запросы | < 1%/мин |
| `http_server_requests_total{status="403"}` | Forbidden запросы | < 1%/мин |

### 1.3 Rate Limiting
| Метрика | Описание | Целевое значение |
|---------|----------|-----------------|
| `rate_limiter_requests_total` | Запросы с rate limit | Counter |
| `rate_limiter_rejected_total` | Отклонённые запросы | < 100/мин |
| `rate_limiter_tokens_remaining` | Оставшиеся токены | Gauge |

### 1.4 Аудит и логирование
| Метрика | Описание | Целевое значение |
|---------|----------|-----------------|
| `security_audit_events_total` | События аудита | Counter |
| `security_alerts_total` | Алерты безопасности | Counter |
| `security_events_lag` | Лаг обработки событий | < 100ms |

---

## Обзор

Документ описывает стратегию мониторинга и наблюдаемости (observability) AutoDev Marketplace, включая метрики, логирование, трассировку и дашборды.

---

## Общие принципы

### 4 золотых сигнала (Google)
1. **Latency** — время обработки запроса
2. **Traffic** — количество запросов
3. **Errors** — количество ошибок
4. **Saturation** — загрузка ресурсов

### O11y Stack
- **Prometheus** — сбор метрик
- **Grafana** — визуализация
- **Loki** — централизованное логирование
- **Tempo** — распределённая трассировка
- **Alloy** — агент телеметрии

---

## Метрики (Prometheus)

### Spring Boot Actuator метрики

#### Основные метрики
| Метрика | Описание | Тип |
|---------|----------|-----|
| `http_server_requests_seconds_count` | Количество HTTP запросов | Counter |
| `http_server_requests_seconds_sum` | Суммарное время обработки | Summary |
| `http_server_requests_seconds_max` | Максимальное время | Gauge |
| `jvm_memory_used_bytes` | Использование памяти JVM | Gauge |
| `jvm_gc_pause_seconds` | Время GC пауз | Summary |
| `tomcat_threads_busy` | Занятые потоки Tomcat | Gauge |
| `tomcat_sessions_active` | Активные сессии Tomcat | Gauge |

#### Метрики Kafka
| Метрика | Описание | Тип |
|---------|----------|-----|
| `kafka_producer_records_success_total` | Успешные сообщения | Counter |
| `kafka_producer_records_error_total` | Ошибки отправки | Counter |
| `kafka_consumer_records_consumed_total` | Прочитанные сообщения | Counter |
| `kafka_consumer_records_lag` | Лаг консьюмера | Gauge |

#### Метрики PostgreSQL
| Метрика | Описание | Тип |
|---------|----------|-----|
| `postgresql_database_size_bytes` | Размер БД | Gauge |
| `postgresql_connections_active` | Активные соединения | Gauge |
| `postgresql_queries_total` | Количество запросов | Counter |
| `postgresql_locks_total` | Блокировки | Gauge |

### Custom метрики

#### Auth Service
```java
@Service
public class AuthServiceMetrics {
    
    private final Counter loginAttemptsCounter;
    private final Counter loginFailuresCounter;
    private final Timer tokenValidationTimer;
    
    public AuthServiceMetrics(MeterRegistry meterRegistry) {
        this.loginAttemptsCounter = Counter.builder("auth_login_attempts_total")
            .description("Total login attempts")
            .register(meterRegistry);
        
        this.loginFailuresCounter = Counter.builder("auth_login_failures_total")
            .description("Total failed login attempts")
            .register(meterRegistry);
        
        this.tokenValidationTimer = Timer.builder("auth_token_validation_seconds")
            .description("Token validation duration")
            .register(meterRegistry);
    }
    
    public void recordLoginAttempt(boolean success) {
        loginAttemptsCounter.increment();
        if (!success) {
            loginFailuresCounter.increment();
        }
    }
    
    public <T> T validateToken(String token, Callable<T> task) throws Exception {
        return tokenValidationTimer.recordCallable(task);
    }
}
```

#### Order Service
```java
@Service
public class OrderServiceMetrics {
    
    private final Summary orderProcessingSummary;
    private final Counter orderCreatedCounter;
    private final Counter orderFailedCounter;
    
    public OrderServiceMetrics(MeterRegistry meterRegistry) {
        this.orderProcessingSummary = Summary.builder("order_processing_seconds")
            .description("Order processing duration")
            .register(meterRegistry);
        
        this.orderCreatedCounter = Counter.builder("order_created_total")
            .description("Total created orders")
            .register(meterRegistry);
        
        this.orderFailedCounter = Counter.builder("order_failed_total")
            .description("Total failed orders")
            .register(meterRegistry);
    }
    
    public <T> T processOrder(Callable<T> task) throws Exception {
        return orderProcessingSummary.recordCallable(task);
    }
}
```

---

## Логирование (Loki)

### Уровни логов
| Уровень | Описание | Использование |
|---------|----------|---------------|
| `TRACE` | Детальное трассирование | Development |
| `DEBUG` | Отладочная информация | Development |
| `INFO` | Информационные сообщения | Production |
| `WARN` | Предупреждения | Production |
| `ERROR` | Ошибки | Production |
| `FATAL` | Критические ошибки | Production |

### Формат логов (JSON)
```json
{
  "timestamp": "2026-06-03T10:30:00.123Z",
  "level": "INFO",
  "logger": "com.autodev.order.OrderService",
  "message": "Order created successfully",
  "trace_id": "abc123def456",
  "span_id": "span789",
  "correlation_id": "corr-xyz",
  "user_id": 123,
  "order_id": 456,
  "request_id": "req-789",
  "host": "order-service-1",
  "service": "order-service",
  "environment": "production"
}
```

### Пример конфигурации
```yaml
logging:
  level:
    root: INFO
    com.autodev: INFO
    org.hibernate.SQL: WARN
    org.springframework: INFO
  pattern:
    console: "%d{yyyy-MM-dd'T'HH:mm:ss.SSS'Z',UTC} %clr(%p) %clr(%logger{36}){cyan} %X{trace_id,span_id,correlation_id} %m%n%wEx"
    file: "%d{yyyy-MM-dd'T'HH:mm:ss.SSS'Z',UTC} %p %logger{36} %X{trace_id,span_id,correlation_id} %m%n%wEx"
```

### Loki query examples
```logql
# Все ошибки за последний час
{service="order-service", level="ERROR"} |= "" [1h]

# Запросы медленнее 500ms
{service="api-gateway"} | json | latency_seconds > 0.5

# Количество запросов по сервисам
sum by(service) (count_over_time({job=~"autodev-.*"}[1m]))

# Аномалии (rate > 100 req/s)
sum by(service) (rate(http_requests_total[1m])) > 100
```

---

## Трассировка (Tempo)

### Распределённая трассировка
```
Client → API Gateway → Auth Service → User Service → PostgreSQL
    ↓          ↓             ↓            ↓            ↓
trace_id: abc123
span_id:   -           span1        span2        span3
```

### Spring Cloud Sleuth
```yaml
spring:
  sleuth:
    propagation:
      type: w3c
    sampler:
      probability: 1.0  # 100% трассировка
    propagation:
      b3:
        enabled: true
```

### Метрики трассировки
| Метрика | Описание |
|---------|----------|
| `http_server_requests_seconds_count` | Количество запросов |
| `http_server_requests_seconds_sum` | Суммарное время |
| `http_server_requests_seconds_max` | Максимальное время |
| `spring_webmvc_controller_invocations_total` | Вызовы контроллеров |

### Tempo query examples
```promql
# Трейсы медленнее 500ms
{service="order-service", duration>500ms}

# Трейсы с ошибками
{service="payment-service", error="true"}

# Трейсы по span
{service="api-gateway", span="auth-service"}
```

---

## Grafana дашборды

### Главный дашборд
| Секция | Метрики |
|--------|---------|
| **System Overview** | CPU, Memory, Disk, Network |
| **API Gateway** | RPS, Latency, Errors, Circuit Breakers |
| **Auth Service** | Login Attempts, Token Validations, Errors |
| **Order Service** | Orders Created, Processing Time, Failures |
| **Database** | Connections, Queries, Replication Lag |
| **Kafka** | Producer/Consumer Metrics, Lag, Throughput |

### Пользовательские дашборды

#### Dashboard: Order Processing
```json
{
  "panels": [
    {
      "title": "Orders per Minute",
      "expr": "sum by(service) (rate(order_created_total[1m]))",
      "type": "graph"
    },
    {
      "title": "Order Processing Time",
      "expr": "histogram_quantile(0.95, rate(order_processing_seconds_bucket[5m]))",
      "type": "graph"
    },
    {
      "title": "Order Failures",
      "expr": "sum by(service) (rate(order_failed_total[5m]))",
      "type": "graph"
    }
  ]
}
```

#### Dashboard: System Health
```json
{
  "panels": [
    {
      "title": "CPU Usage",
      "expr": "100 - (avg by(instance) (rate(node_cpu_seconds_total{mode=\"idle\"}[5m])) * 100)",
      "type": "graph"
    },
    {
      "title": "Memory Usage",
      "expr": "100 * (1 - (node_memory_MemAvailable_bytes / node_memory_MemTotal_bytes))",
      "type": "graph"
    },
    {
      "title": "Disk Usage",
      "expr": "100 * (1 - (node_filesystem_avail_bytes / node_filesystem_size_bytes))",
      "type": "graph"
    }
  ]
}
```

---

## Alertmanager конфигурация

### Алерты Prometheus
```yaml
groups:
- name: autodev-alerts
  rules:
  - alert: HighErrorRate
    expr: sum(rate(http_requests_total{status=~"5.."}[5m])) / sum(rate(http_requests_total[5m])) > 0.05
    for: 5m
    labels:
      severity: critical
    annotations:
      summary: "High error rate detected"
      description: "Error rate is {{ $value | humanizePercentage }} for the last 5 minutes"

  - alert: ServiceDown
    expr: up{job=~"autodev-.*"} == 0
    for: 1m
    labels:
      severity: critical
    annotations:
      summary: "Service is down"
      description: "Service {{ $labels.instance }} is down"

  - alert: HighLatency
    expr: histogram_quantile(0.95, rate(http_server_requests_seconds_bucket[5m])) > 0.5
    for: 10m
    labels:
      severity: warning
    annotations:
      summary: "High latency detected"
      description: "p95 latency is {{ $value }}s for the last 10 minutes"
```

### Alertmanager routes
```yaml
route:
  receiver: 'pagerduty-critical'
  group_by: ['alertname', 'service']
  routes:
  - match:
      severity: critical
    receiver: 'pagerduty-critical'
  - match:
      severity: warning
    receiver: 'slack-warnings'

receivers:
- name: 'pagerduty-critical'
  pagerduty_configs:
  - service_key: ${PAGERDUTY_SERVICE_KEY}

- name: 'slack-warnings'
  slack_configs:
  - api_url: ${SLACK_WEBHOOK_URL}
    channel: '#alerts'
    send_resolved: true
```

---

## Metalog (Alloy) агент

### Конфигурация Alloy
```alloy
// Collect logs from Docker containers
log_shipper "docker" {
  forward_to = [loki.write.logs]
  
  discovery "docker" {
    hosts = ["unix:///var/run/docker.sock"]
  }
}

// Collect metrics from Kubernetes
metric_scraper "k8s" {
  forward_to = [prometheus.remote_write.metrics]
  
  discovery "kubernetes" {
    roles = ["pod", "service", "node"]
  }
}

// Export traces to Tempo
trace_exporter "tempo" {
  endpoint = "tempo:4317"
  insecure = true
}
```

---

## Мониторинг Kafka

### Метрики Kafka
```promql
# Consumer lag
kafka_consumer_group_lag

# Producer throughput
kafka_producer_record_send_rate

# Broker throughput
kafka_broker_throughput_in_rate

# Replication lag
kafka_topic_replica_under_replicated_partitions
```

### Алерты Kafka
```yaml
- alert: HighConsumerLag
  expr: kafka_consumer_group_lag > 10000
  for: 5m
  labels:
    severity: warning
  annotations:
    summary: "High consumer lag detected"
    description: "Consumer lag is {{ $value }} messages"

- alert: UnderReplicatedPartitions
  expr: kafka_topic_replica_under_replicated_partitions > 0
  for: 1m
  labels:
    severity: critical
  annotations:
    summary: "Under-replicated partitions detected"
    description: "{{ $value }} partitions are under-replicated"
```

---

## Мониторинг PostgreSQL

### Метрики PostgreSQL
```promql
# Active connections
postgresql_conn_count

# Connection usage ratio
postgresql_conn_count / postgresql_conn_max

# Slow queries
postgresql_query_duration_seconds > 1

# Replication lag
postgresql_replication_lag_seconds
```

### Алерты PostgreSQL
```yaml
- alert: HighConnectionCount
  expr: postgresql_conn_count / postgresql_conn_max > 0.8
  for: 5m
  labels:
    severity: warning
  annotations:
    summary: "High connection count"
    description: "Connection pool usage is {{ $value | humanizePercentage }}"

- alert: ReplicationLag
  expr: postgresql_replication_lag_seconds > 5
  for: 2m
  labels:
    severity: critical
  annotations:
    summary: "Replication lag detected"
    description: "Replication lag is {{ $value }} seconds"
```

---

## Мониторинг Redis

### Метрики Redis
```promql
# Memory usage
redis_memory_used_bytes / redis_memory_max_bytes

# Keyspace hits/misses
redis_keyspace_hits_total / redis_keyspace_misses_total

# Connected clients
redis_connected_clients
```

### Алерты Redis
```yaml
- alert: HighMemoryUsage
  expr: redis_memory_used_bytes / redis_memory_max_bytes > 0.8
  for: 5m
  labels:
    severity: warning
  annotations:
    summary: "High Redis memory usage"
    description: "Memory usage is {{ $value | humanizePercentage }}"

- alert: HighMissRate
  expr: redis_keyspace_misses_total / (redis_keyspace_hits_total + redis_keyspace_misses_total) > 0.2
  for: 5m
  labels:
    severity: warning
  annotations:
    summary: "High Redis miss rate"
    description: "Cache miss rate is {{ $value | humanizePercentage }}"
```

---

## SLA/SLO метрики

### SLO цели
| SLO | Целевое значение | Измерение |
|-----|-----------------|-----------|
| API Availability | 99.9% | Uptime |
| API Latency p95 | <500ms | Tempo |
| API Latency p99 | <1000ms | Tempo |
| Auth Latency p95 | <100ms | Tempo |
| Search Latency p95 | <200ms | Tempo |

### SLA метрики
| Компонент | SLA |
|-----------|-----|
| API Gateway | 99.95% |
| Auth Service | 99.9% |
| Order Service | 99.9% |
| Payment Service | 99.99% |
| PostgreSQL | 99.9% |
| Redis | 99.9% |

---

## Заключение

Стратегия мониторинга и наблюдаемости:
- **4 золотых сигнала** для всех сервисов
- **Prometheus** для метрик с алертингом
- **Grafana** для визуализации и дашбордов
- **Loki** для централизованного логирования
- **Tempo** для распределённой трассировки
- **Alloy** для сбора телеметрии
- **SLA/SLO** цели для критичных компонентов
- **Alertmanager** для уведомлений

Система полностью наблюдаема с покрытием всех уровней стека.
