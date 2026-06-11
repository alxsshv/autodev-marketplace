# Search Service

**Версия документа:** 1.0  
**Дата создания:** 2026-06-04  
**Последнее обновление:** 2026-06-04

---

## Обзор

Search Service — это сервис полнотекстового поиска и фильтрации товаров. Он обеспечивает быстрый и точный поиск автозапчастей с расширенной фильтрацией по множеству критериев, автодополнение запросов и рекомендательный поиск.

---

## Бизнес-функция

Search Service обеспечивает:
- Полнотекстовый поиск товаров по названию, описанию, артикулу
- Расширенная фильтрация по цене, состоянию, наличию, региону, рейтингу
- Автодополнение (typeahead) для улучшения UX
- Рекомендательный поиск на основе истории пользователя
- История поиска и часто используемые запросы
- Группировка результатов по производителям и категориям

---

## API endpoints

### Basic Search

| Method | Path | Описание | RBAC |
|--------|------|----------|------|
| `GET` | `/api/v1/search` | Полнотекстовый поиск | BUYER |
| `GET` | `/api/v1/search/suggest` | Автодополнение (typeahead) | BUYER |
| `GET` | `/api/v1/search/categories` | Поиск по категориям | BUYER |

### Advanced Search

| Method | Path | Описание | RBAC |
|--------|------|----------|------|
| `GET` | `/api/v1/search/products` | Поиск товаров с фильтрацией | BUYER |
| `GET` | `/api/v1/search/filters` | Получение доступных фильтров | BUYER |

### Filters

| Method | Path | Описание | RBAC |
|--------|------|----------|------|
| `GET` | `/api/v1/search/filters/price` | Фильтр по цене | BUYER |
| `GET` | `/api/v1/search/filters/condition` | Фильтр по состоянию (новый/б/у) | BUYER |
| `GET` | `/api/v1/search/filters/availability` | Фильтр по наличию | BUYER |
| `GET` | `/api/v1/search/filters/location` | Фильтр по региону | BUYER |
| `GET` | `/api/v1/search/filters/rating` | Фильтр по рейтингу | BUYER |
| `GET` | `/api/v1/search/filters/seller` | Фильтр по продавцу | BUYER |

### Recommendations

| Method | Path | Описание | RBAC |
|--------|------|----------|------|
| `GET` | `/api/v1/search/recommendations` | Рекомендательный поиск | BUYER |
| `GET` | `/api/v1/search/recent` | Последние поисковые запросы | BUYER |
| `GET` | `/api/v1/search/trending` | Популярные запросы | BUYER |

---

## Технические детали

### Конфигурация (application.yml)
```yaml
server:
  port: 8086

spring:
  application:
    name: search-service
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
    implementation("org.springframework.boot:spring-boot-starter-data-elasticsearch")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.cloud:spring-cloud-starter-consul-discovery")
    implementation("org.springframework.cloud:spring-cloud-starter-consul-config")
    implementation("org.springframework.kafka:spring-kafka")
    
    implementation("io.micrometer:micrometer-registry-prometheus")
}
```

---

## Модель данных (Elasticsearch)

### Индексы

#### `products`
```json
{
  "mappings": {
    "properties": {
      "id": { "type": "keyword" },
      "sku": { "type": "keyword" },
      "name": { 
        "type": "text", 
        "analyzer": "russian",
        "fields": {
          "keyword": { "type": "keyword" }
        }
      },
      "description": { 
        "type": "text", 
        "analyzer": "russian" 
      },
      "category_id": { "type": "keyword" },
      "brand_id": { "type": "keyword" },
      "price": { "type": "scaled_float", "scaling_factor": 100 },
      "condition": { "type": "keyword" },
      "availability": { "type": "keyword" },
      "location": { "type": "keyword" },
      "seller_id": { "type": "keyword" },
      "rating": { "type": "float" },
      "reviews_count": { "type": "integer" },
      "created_at": { "type": "date" }
    }
  }
}
```

#### `categories`
```json
{
  "mappings": {
    "properties": {
      "id": { "type": "keyword" },
      "name": { "type": "text", "analyzer": "russian" },
      "slug": { "type": "keyword" },
      "parent_id": { "type": "keyword" }
    }
  }
}
```

#### `suggestions`
```json
{
  "mappings": {
    "properties": {
      "query": { 
        "type": "completion",
        "analyzer": "russian",
        "search_analyzer": "russian"
      },
      "popularity": { "type": "integer" }
    }
  }
}
```

---

## Архитектурные решения

### Elasticsearch Integration
- **Использование:** Полнотекстовый поиск с поддержкой русского языка
- **Анализаторы:** `russian` для текста, `keyword` для фильтрации
- **Синхронизация:** Асинхронная через Kafka (событие product.created/updated/deleted)

### Full-Text Search
- **Логика:** Full-text search по названию и описанию
- **Boosting:** Повышение релевантности для точного совпадения в названии
- **Typo tolerance:** Исправление опечаток через fuzzy search

### Filtering
- **Filter aggregation:** Эффективная фильтрация без влияния на релевантность
- **Range queries:** Фильтрация по цене, рейтингу
- **Term queries:** Точное совпадение (brand, category, condition)

### Autocomplete
- **Elasticsearch Completion Suggester:** Быстрое автодополнение
- **Popularity ranking:** Учет популярности запросов

### Recommendations
- **Collaborative filtering:** На основе истории покупок
- **Content-based:** На основе истории просмотров
- **Trending:** На основе популярности

---

## Паттерны проектирования

### Repository Pattern
Использование Elasticsearch repository для доступа к данным.

### Caching Pattern
- Кэш популярных запросов (1 час TTL)
- Кэш результатов для повторных запросов (5 минут TTL)

---

## Межсервисное взаимодействие

### Синхронное (REST API)
- Search Service → Elasticsearch API (поиск, агрегации)

### Асинхронное (Kafka)
- Search Service ← Kafka (события: product.created, product.updated, product.deleted)
- Search Service → Kafka (события: search.query, search.result)

---

## Безопасность

### Аутентификация
- JWT токены в заголовке `Authorization: Bearer {token}`
- Проверка токена через Auth Service

### Авторизация
- Публичный доступ к поиску товаров
- Персонализированные рекомендации только для авторизованных пользователей

---

## Метрики

| Метрика | Описание | Тип |
|---------|----------|-----|
| `search_queries_total` | Количество поисковых запросов | Counter |
| `search_results_total` | Количество найденных результатов | Counter |
| `autocomplete_queries_total` | Количество запросов автодополнения | Counter |
| `recommendation_requests_total` | Количество запросов рекомендаций | Counter |
| `elasticsearch_queries_total` | Количество запросов к Elasticsearch | Counter |

---

## SLA/SLO

| Метрика | Целевое значение | Измерение |
|---------|-----------------|-----------|
| Доступность | 99.9% | Uptime (Prometheus) |
| Latency (p95) | <100 мс | Tempo traces |
| Latency (p99) | <300 мс | Tempo traces |
| Ошибки (p99) | <0.5% | Prometheus errors |

---

## Deployment

### Kubernetes
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: search-service
spec:
  replicas: 2
  template:
    spec:
      containers:
      - name: search-service
        image: autodev/search-service:latest
        ports:
        - containerPort: 8086
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
- Полнотекстовый поиск
- Фильтрация результатов
- Автодополнение

### Integration Tests
- Testcontainers для Elasticsearch, Kafka
- Симуляция сценариев поиска

---

## Мониторинг

### Grafana Dashboard
- Search Service Overview
- Query Performance
- Popular Searches
- Recommendation Stats

### Алерты
- `HighSearchLatency`: p95 >500ms за 10 минут
- `SearchErrors`: >1% ошибок за 5 минут
- `ServiceDown`: Сервис недоступен

---

## Риски и ограничения

### Текущие риски
1. **Elasticsearch dependency** — поиск полностью зависит от Elasticsearch
   - **Mitigation:** Локальный кэш популярных запросов

2. **Index refresh delay** — изменения в индексе не отражаются мгновенно
   - **Mitigation:** Оповещение пользователя о возможной задержке

3. **Complex queries** — сложные фильтрации могут замедлять поиск
   - **Mitigation:** Лимитирование количества фильтров и их сложности

---

## План улучшений

### Short-term (1-2 недели)
- [ ] Реализовать полную фильтрацию
- [ ] Добавить сортировку по цене, рейтингу, дате
- [ ] Интеграция с историей поиска пользователя

### Medium-term (1-2 месяца)
- [ ] Гео-поиск (по региону продавца)
- [ ] Поиск по VIN-коду
- [ ] Продвинутая рекомендательная система

### Long-term (3-6 месяцев)
- [ ] Машинное обучение для релевантности
- [ ] Визуальный поиск (по изображению)
- [ ] Голосовой поиск

---

## Контакты

- **Owner:** Backend Team
- **Slack:** #search-service
- **Emergency:** #incident

---

## См. также

- [Консолидация сервисов](../service-consolidation.md) — детали консолидации
- [Системный обзор](../system-overview.md) — обзор архитектуры
- [OpenAPI спецификация](../api-specification/README.md) — API документация
- [ADR-0001](../architecture-decision-records/adr-0001-consolidation.md) — консолидация сервисов
