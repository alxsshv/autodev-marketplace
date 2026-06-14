# AutoDev Marketplace — Глоссарий и соглашения

**Версия документа:** 1.0  
**Дата создания:** 2026-06-03  
**Последнее обновление:** 2026-06-03

---

## Глоссарий терминов

### Общие термины

| Термин | Описание |
|--------|----------|
| **Microservice** | Небольшой независимый сервис, реализующий одну бизнес-функцию |
| **API Gateway** | Единая точка входа для всех клиентов, маршрутизирующая запросы к микросервисам |
| **Service Discovery** | Механизм автоматического обнаружения и регистрации микросервисов |
| **Circuit Breaker** | Паттерн предотвращения каскадных сбоев при отказе зависимых сервисов |
| **Rate Limiting** | Ограничение количества запросов к сервису для защиты от перегрузки |
| **CQRS** | Паттерн разделения операций чтения (Query) и записи (Command) |
| **Event Sourcing** | Паттерн хранения состояния системы как последовательности событий |
| **Saga Pattern** | Паттерн управления распределёнными транзакциями через последовательность локальных транзакций |
| **Database per Service** | Паттерн, при котором каждый микросервис имеет свою базу данных |
| **Idempotency** | Свойство операции, при котором повторные вызовы не изменяют результат |
| **Webhook** | HTTP callback, отправляемый внешней системой при наступлении события |
| **JWT (JSON Web Token)** | Открытый стандарт для создания токенов аутентификации |
| **OAuth2** | Протокол авторизации для делегирования доступа |
| **OpenID Connect** | Протокол аутентификации поверх OAuth2 |
| **Kafka** | Распределённая платформа потоковой передачи данных |
| **Elasticsearch** | Распределённый поисковый сервер на базе Lucene |
| **Redis** | База данных in-memory для кэширования и быстрого доступа |

---

### Бизнес-термины

| Термин | Описание |
|--------|----------|
| **Автозапчасть** | Компонент автомобиля, подлежащий замене при поломке |
| **VIN (Vehicle Identification Number)** | Уникальный идентификатор автомобиля из 17 символов |
| **Артикул** | Каталожный номер запчасти производителя |
| **Кросс-номер** | Альтернативный номер запчасти другого производителя |
| **Эскроу-счёт** | Безопасная сделка, при которой деньги резервируются до подтверждения получения |
| **Рейтинг продавца** | Средняя оценка продавца на основе отзывов покупателей |
| **Модерация** | Проверка контента на соответствие правилам платформы |
| **Прайс-лист** | Файл с ценами и наличием товаров для загрузки продавцом |
| **Доставка ТК** | Доставка через транспортную компанию (СДЭК, Boxberry и др.) |
| **Гарантия** | Обязательство продавца по бесплатной замене товара в течение определённого срока |
| **Возврат** | Возвращение товара продавцу с возвратом денежных средств |

---

### Технические термины

| Термин | Описание |
|--------|----------|
| **Spring Boot** | Фреймворк для создания автономных приложений на Java |
| **Spring Cloud** | Набор инструментов для построения микросервисов |
| **Resilience4j** | Библиотека для реализации паттернов отказоустойчивости |
| **Liquibase** | Инструмент для управления миграциями базы данных |
| **JUnit 5** | Фреймворк для модульного тестирования Java |
| **Testcontainers** | Библиотека для интеграционного тестирования с реальными контейнерами |
| **Docker** | Платформа для контейнеризации приложений |
| **Kubernetes** | Оркестратор контейнеров |
| **Consul** | Service Discovery и KV-store от HashiCorp |
| **Keycloak** | Централизованная система аутентификации и авторизации (единственный источник правды для ролей) |
| **Database per Service** | Паттерн, при котором каждый микросервис имеет свою базу данных |
| **Single Source of Truth** | Ключевой принцип: Keycloak является единственным источником правды для ролей пользователей (в PostgreSQL нет таблиц auth.roles, auth.permissions, auth.role_permissions) |
| **MinIO** | Объектное хранилище, совместимое с AWS S3 |
| **Prometheus** | Система сбора и хранения метрик |
| **Grafana** | Инструмент для визуализации метрик и логов |
| **Loki** | Система централизованного логирования |
| **Tempo** | Система распределённой трассировки |
| **OpenAPI 3.0** | Спецификация для описания REST API |
| **gRPC** | Современный RPC-фреймворк от Google |
| **WebSocket** | Протокол для двунаправленной коммуникации в реальном времени |

---

## Соглашения

### Именование

#### Микросервисы
- **Формат:** `xxx-service` (например, `api-gateway`, `auth-service`, `platform-service`, `order-service`)
- **Примечание:** Используется 8 консолидированных сервисов для MVP (см. `service-consolidation.md`)

#### Базы данных
- **Формат:** `services-database` (основная БД), `keycloak-database` (БД Keycloak)
- **Имена схем:** `auth`, `catalog`, `order_service`, `payment_service`, `communication_service`, `platform_service`

**Примечание по схеме `auth`:** Схема `auth` содержит только аутентификационные данные пользователей (таблица `users` с полями: id, keycloak_user_id, email, enabled). Все бизнес-данные пользователя хранятся в схеме `platform_service.users`.

**ВАЖНО:** Роли пользователей хранятся исключительно в Keycloak. В PostgreSQL нет таблиц для хранения ролей (`auth.roles`, `auth.permissions`, `auth.role_permissions` не создаются для MVP).

**ВАЖНО:** В таблице `auth.users` НЕТ поля `role`. Роли хранятся только в Keycloak и передаются в JWT токене.

**ВАЖНО:** Auth Service НЕ предоставляет endpoints для управления ролями. Управление ролями осуществляется только через Keycloak Admin Console (http://localhost:8090/admin) или Keycloak Admin API (http://localhost:8090/admin/realms/autodev/roles).

**Примечание:** Search Service не использует PostgreSQL напрямую (только Elasticsearch индексы), API Gateway не использует БД (только маршрутизация)

#### Liquibase миграции
- **Формат:** `services/{service-name}/src/main/resources/db/changelog/v{version}/`
- **Структура:** Каждый сервис имеет свою директорию миграций с версией `v1.0.0` для MVP
- **Master changelog:** `services/{service-name}/src/main/resources/db/changelog/master.yaml` (YAML формат)
- **Миграции:** SQL файлы в директории `v1.0.0/`
- **Пример:** `services/auth-service/src/main/resources/db/changelog/v1.0.0/03-06-2026-create-table-users.sql`

**Примечание:** Использование master changelog файлов позволяет объединить все миграции для каждого сервиса в одну логическую версию `v1.0.0` для MVP. Для master changelog используется YAML формат из-за его читаемости и компактности.

#### Таблицы
- **Формат:** `schema.table_name` (например, `auth.users`, `order_service.orders`)
- **Имена:** snake_case (например, `user_profile`, `order_items`)

#### Redis ключи
- **Формат:** `service:type:id` (например, `user:session:123`, `order:cart:456`)
- **Типы:** `session`, `cache`, `queue`, `lock`, `counter`

#### Kafka топики
- **Формат:** `service.event-type` (например, `auth.user_registered`, `platform.user_profile_updated`, `order.order_created`, `payment.payment_completed`)
- **Структура:** `domain.verb.noun` или `domain.noun.event`

#### API endpoints
- **Формат:** `/api/v1/{resource}` (например, `/api/v1/users`, `/api/v1/orders`)
- **Параметры:** snake_case (например, `user_id`, `order_id`)

#### Kubernetes ресурсы
- **Deployment:** `service-name`
- **Service:** `service-name`
- **ConfigMap:** `service-name-config`
- **Secret:** `service-name-secret`

---

### Версионирование

#### API
- **Формат:** `/api/v{major}` (например, `/api/v1`)
- **Изменения:** backward incompatible changes → new major version

#### Микросервисы
- **Семантическое версионирование:** `major.minor.patch`
- **Пример:** `1.2.3` (breaking change, new feature, bug fix)

#### База данных
- **Liquibase:** `v{major}.{minor}.{patch}/date-description.sql`
- **Пример:** `v1.0.0/03-06-2026-create-table-users.sql`

---

### Структура проекта

#### Генеральная схема
```
autodev-marketplace/
├── services/                    # Микросервисы
│   ├── api-gateway/            # API Gateway
│   ├── auth-service/           # Аутентификация
│   │   └── src/main/resources/db/changelog/
│   │       ├── master.xml      # Главный файл миграции для auth-service
│   │       └── v1.0.0/         # Версия миграций для MVP
│   │           ├── 01-create-users.sql
│   │           ├── 02-create-oauth-providers.sql
│   │           └── ...
│   ├── catalog-service/        # Каталог товаров
│   ├── order-service/          # Заказы
│   ├── search-service/         # Поиск
│   ├── payment-service/        # Оплата
│   ├── communication-service/  # Коммуникация
│   ├── platform-service/       # Платформа (Users + Moderation + Reviews + Analytics + Admin)
│   │   └── src/main/resources/db/changelog/
│   │       ├── master.xml      # Главный файл миграции для platform-service
│   │       └── v1.0.0/         # Версия миграций для MVP
│   │           └── ...
│   └── ...
├── docs/                        # Документация
│   └── architecture/           # Архитектурная документация
│       ├── roadmap.md
│       ├── system-context.md
│       └── ...
├── infrastructure/              # Инфраструктура
│   ├── service-db/
│   │   └── init/               # Инициализация БД для Docker
│   │       └── init-schemas.sql
│   ├── prometheus/
│   ├── loki/
│   └── ...
├── docker-compose.yaml          # Локальная инфраструктура
└── build.gradle.kts             # Корневой Gradle файл
```

**Примечание по миграциям:**
Для MVP используется структура, где каждый сервис имеет свою директорию миграций с версией `v1.0.0`. Это соответствует паттерну "Database per Service" в микросервисной архитектуре. Каждая директория `v1.0.0` содержит набор SQL файлов, которые применяются в алфавитном порядке через master changelog файл.

#### Структура микросервиса
```
service-name/
├── src/
│   ├── main/
│   │   ├── java/com/autodev/servicename/
│   │   │   ├── ServiceNameApplication.java
│   │   │   ├── controller/
│   │   │   ├── service/
│   │   │   ├── repository/
│   │   │   ├── entity/
│   │   │   └── dto/
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── db/changelog/
│   │       │   ├── master.yaml     # Главный файл миграции (YAML формат)
│   │       │   └── v1.0.0/         # Версия миграций для MVP
│   │       │       └── *.sql       # SQL файлы миграций
│   │       └── liquibase/
│   └── test/
│       └── java/com/autodev/servicename/
├── Dockerfile
├── build.gradle.kts
└── README.md
```

---

### Типы данных

#### Стандартные типы
| Java | PostgreSQL | Описание |
|------|------------|----------|
| `Long` | `BIGSERIAL` | Идентификатор |
| `String` | `VARCHAR(255)` | Текст |
| `LocalDateTime` | `TIMESTAMP` | Временная метка |
| `Boolean` | `BOOLEAN` | Логическое значение |
| `BigDecimal` | `NUMERIC(10,2)` | Деньги |
| `Integer` | `INTEGER` | Целое число |

#### Перечисления
```java
public enum Role {
    BUYER,
    SELLER,
    MODERATOR,
    ADMIN
}

public enum OrderStatus {
    PENDING,
    PROCESSING,
    SHIPPED,
    DELIVERING,
    READY,
    COMPLETED,
    CANCELLED,
    RETURNED
}
```

---

### Коды HTTP статусов

| Код | Описание | Использование |
|-----|----------|---------------|
| `200` | OK | Успешный GET/PUT/PATCH |
| `201` | Created | Успешное создание (POST) |
| `204` | No Content | Успешное удаление (DELETE) |
| `400` | Bad Request | Невалидные данные |
| `401` | Unauthorized | Отсутствует или неверный токен |
| `403` | Forbidden | Недостаточно прав |
| `404` | Not Found | Ресурс не найден |
| `409` | Conflict | Конфликт (дубликат) |
| `429` | Too Many Requests | Превышен лимит запросов |
| `500` | Internal Server Error | Внутренняя ошибка |
| `503` | Service Unavailable | Сервис недоступен |

---

### Коды ошибок API

#### Формат ответа ошибки
```json
{
  "error": {
    "code": "USER_NOT_FOUND",
    "message": "Пользователь с id=123 не найден",
    "timestamp": "2026-06-03T10:30:00Z",
    "details": {
      "user_id": 123
    }
  }
}
```

#### Типы ошибок
| Код | Описание | Уровень |
|-----|----------|---------|
| `VALIDATION_ERROR` | Ошибка валидации | Client |
| `AUTHENTICATION_ERROR` | Ошибка аутентификации | Client |
| `AUTHORIZATION_ERROR` | Ошибка авторизации | Client |
| `RESOURCE_NOT_FOUND` | Ресурс не найден | Client |
| `CONFLICT_ERROR` | Конфликт | Client |
| `INTERNAL_ERROR` | Внутренняя ошибка | Server |
| `SERVICE_UNAVAILABLE` | Сервис недоступен | Server |

---

### Именование констант

#### Конфигурация
```yaml
spring:
  application:
    name: user-service
    
management:
  endpoints:
    web:
      exposure:
        include: health,info,prometheus
```

#### Переменные окружения
```env
# Формат: SERVICE_NAME_CONFIG
USER_SERVICE_DB_HOST=localhost
USER_SERVICE_DB_PORT=5438
USER_SERVICE_DB_USER=autodev
USER_SERVICE_DB_PASSWORD=autodev
```

---

### Комментарии в коде

#### Типы комментариев
1. **Javadoc** — для публичных классов и методов
2. **Inline** — для сложных алгоритмов
3. **Todo/Fixme** — для отслеживания задач

#### Пример Javadoc
```java
/**
 * Проверяет, доступна ли запчасть для заказа.
 *
 * @param productId ID товара
 * @param quantity  Количество
 * @return true если доступна, false иначе
 */
boolean isProductAvailable(Long productId, Integer quantity);
```

---

### Git соглашения

#### Структура веток
```
main              # Production
├── develop         # Integration
│   ├── feature/*   # Новые функции
│   ├── bugfix/*    # Исправления багов
│   └── hotfix/*    # Срочные исправления
└── release/*       # Релизы
```

#### Соглашение о коммитах (Conventional Commits)
```
<type>: <description>

[optional body]

<type> = feat | fix | chore | docs | style | refactor | perf | test | build | ci | revert
```

#### Примеры
```
feat: добавлен API для управлени�� заказами
fix: исправлена ошибка резервирования наличия
docs: обновлена документация API
chore: обновлены зависимости
```

---

### Docker соглашения

#### Именование образов
```
<registry>/<project>/<service>:<tag>
autodev/api-gateway:latest
autodev/user-service:v1.2.3
```

#### Метки Docker
```dockerfile
LABEL org.opencontainers.image.title="AutoDev Marketplace API Gateway"
LABEL org.opencontainers.image.description="API Gateway for AutoDev Marketplace"
LABEL org.opencontainers.image.version="1.0.0"
LABEL org.opencontainers.image.revision="abc123"
LABEL org.opencontainers.image.vendor="AutoDev"
```

---

### Kubernetes соглашения

#### Метки подов
```yaml
metadata:
  labels:
    app: api-gateway
    version: v1.0.0
    environment: production
    team: backend
```

#### Аннотации
```yaml
metadata:
  annotations:
    prometheus.io/scrape: "true"
    prometheus.io/port: "8080"
    prometheus.io/path: "/actuator/metrics"
```

---

### Метрики

#### Обязательные метрики
| Метрика | Описание | Единица |
|---------|----------|---------|
| `http_requests_total` | Количество HTTP запросов | count |
| `http_request_duration_seconds` | Время обработки запроса | seconds |
| `database_connections_active` | Активные соединения с БД | count |
| `kafka_consumer_lag` | Лаг консьюмера Kafka | messages |
| `memory_usage_bytes` | Использование памяти | bytes |
| `cpu_usage_percent` | Использование CPU | percent |

---

### Секреты

#### Хранение
- **Не хранить в Git**
- **Использовать Vault или Kubernetes Secrets**
- **Rotating secrets** каждые 90 дней

#### Пример
```yaml
# secret.yaml
apiVersion: v1
kind: Secret
metadata:
  name: auth-service-secret
type: Opaque
stringData:
  database-password: ${DB_PASSWORD}
  redis-password: ${REDIS_PASSWORD}
  keycloak-client-secret: ${KEYCLOAK_SECRET}
```

---

## Заключение

Этот глоссарий и соглашения обеспечивают:
- **Единый язык** для всех участников проекта
- **Консистентность** именования и структуры
- **Понятность** кода и документации
- **Автоматизацию** через стандарты

Все участники команды обязаны следовать этим соглашениям.
