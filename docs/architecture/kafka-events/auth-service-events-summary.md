# Kafka события Auth Service — Единая сводка

**Версия документа:** 1.5  
**Дата создания:** 2026-06-14  
**Последнее обновление:** 2026-06-14

---

## Обзор

Этот документ содержит полную сводку всех Kafka событий, которые генерирует и потребляет Auth Service.

**Важно:** All события синхронизации пользователей между Keycloak и PostgreSQL настроены через **Keycloak Events Provider**. Keycloak отправляет webhook события со следующими названиями: `user_created`, `user_updated`, `user_enabled`, `user_disabled` (snake_case, строчные буквы). Детальная настройка и описание процессов см. в `security/user-registration-architecture.md` и `kafka-events/auth-service-events.md`.

---

## Обновления в версии 1.5

### Добавлены ссылки на детальную документацию:

1. **Дедупликация событий** — стратегия предотвращения обработки дубликатов webhook событий от Keycloak
2. **Dead Letter Queue (DLQ)** — полное описание стратегии обработки невалидных событий
3. **Стратегия обработки ошибок синхронизации** — сценарии для PostgreSQL недоступности, конфликтов ключей, невалидных данных

**Примечание:** Детальная информация по этим разделам доступна в `kafka-events/auth-service-events.md`.

---

## События, генерируемые Auth Service (Publisher)

### Согласованность документации

Все документы синхронизированы и описывают единую архитектуру:

1. **Keycloak** → отправляет webhook события в **Auth Service**
2. **Auth Service** → обновляет **PostgreSQL** и публикует Kafka события
3. **Kafka** → доставляет события другим сервисам (Platform Service, Communication Service)
4. **Platform Service** → обновляет бизнес-профиль пользователя

**Типы событий Keycloak:**
- `user_created` — создание пользователя
- `user_updated` — обновление данных пользователя
- `user_enabled` — активация пользователя
- `user_disabled` — деактивация пользователя

**Kafka события Auth Service:**
- `auth.user.registered` (из `user_created`)
- `auth.user.updated` (из `user_updated`)
- `auth.user.enabled` (из `user_enabled`)
- `auth.user.disabled` (из `user_disabled`)
- `auth.user.deleted` (из `user_deleted`, опционально)

**Рекомендация:** НастройкаEvents Provider осуществляется в Keycloak Admin Console или через Admin API. Подробности см. в `security/user-registration-architecture.md`.

### 1. auth.user.registered

**Источник:** Auth Service  
**Когда:** После создания пользователя в Keycloak и синхронизации в PostgreSQL  
**Kafka Topic:** `auth.user.registered`

**Формат события:**
```json
{
  "event": "auth.user.registered",
  "timestamp": "2026-06-14T10:00:00Z",
  "payload": {
    "id": 1,
    "keycloak_user_id": "123e4567-e89b-12d3-a456-426614174000",
    "email": "user@example.com",
    "enabled": true,
    "created_at": "2026-06-14T10:00:00Z"
  },
  "metadata": {
    "source_service": "auth-service",
    "version": "1.0"
  }
}
```

**Потребители:**
- **Platform Service** — создание профиля пользователя в `platform_service.users`
- **Communication Service** — отправка welcome email

---

### 2. auth.user.updated

**Источник:** Auth Service  
**Когда:** После обновления аутентификационных данных пользователя в Keycloak и синхронизации в PostgreSQL  
**Kafka Topic:** `auth.user.updated`

**Формат события:**
```json
{
  "event": "auth.user.updated",
  "timestamp": "2026-06-14T10:05:00Z",
  "payload": {
    "id": 1,
    "keycloak_user_id": "123e4567-e89b-12d3-a456-426614174000",
    "email": "user.new@example.com",
    "enabled": false,
    "created_at": "2026-06-14T10:00:00Z"
  },
  "metadata": {
    "source_service": "auth-service",
    "version": "1.0"
  }
}
```

**Потребители:**
- **Platform Service** — обновление профиля пользователя
- **Communication Service** — уведомление об изменении данных

---

### 3. auth.user.deleted

**Источник:** Auth Service  
**Когда:** После удаления пользователя из Keycloak и PostgreSQL  
**Kafka Topic:** `auth.user.deleted`

**Формат события:**
```json
{
  "event": "auth.user.deleted",
  "timestamp": "2026-06-14T10:10:00Z",
  "payload": {
    "id": 1,
    "keycloak_user_id": "123e4567-e89b-12d3-a456-426614174000",
    "deleted_at": "2026-06-14T10:10:00Z"
  },
  "metadata": {
    "source_service": "auth-service",
    "version": "1.0"
  }
}
```

**Потребители:**
- **Platform Service** — удаление профиля пользователя
- **Communication Service** — остановка уведомлений

---

### 4. auth.user.enabled

**Источник:** Auth Service  
**Когда:** После включения пользователя (enabled = true)  
**Kafka Topic:** `auth.user.enabled`

**Формат события:**
```json
{
  "event": "auth.user.enabled",
  "timestamp": "2026-06-14T10:15:00Z",
  "payload": {
    "id": 1,
    "keycloak_user_id": "123e4567-e89b-12d3-a456-426614174000",
    "enabled": true,
    "enabled_at": "2026-06-14T10:15:00Z"
  },
  "metadata": {
    "source_service": "auth-service",
    "version": "1.0"
  }
}
```

**Потребители:**
- **Platform Service** — разблокировка профиля
- **Communication Service** — уведомление о восстановлении доступа

---

### 5. auth.user.disabled

**Источник:** Auth Service  
**Когда:** После отключения пользователя (enabled = false)  
**Kafka Topic:** `auth.user.disabled`

**Формат события:**
```json
{
  "event": "auth.user.disabled",
  "timestamp": "2026-06-14T10:20:00Z",
  "payload": {
    "id": 1,
    "keycloak_user_id": "123e4567-e89b-12d3-a456-426614174000",
    "disabled": true,
    "disabled_at": "2026-06-14T10:20:00Z",
    "disabled_by": "admin@example.com",
    "reason": "Policy violation"
  },
  "metadata": {
    "source_service": "auth-service",
    "version": "1.0"
  }
}
```

**Потребители:**
- **Platform Service** — блокировка профиля
- **Communication Service** — ув��домление о блокировке

---

## События, потребляемые Auth Service (Consumer)

### 1. user.profile.created (из Platform Service)

**Источник:** Platform Service  
**Когда:** После создания профиля пользователя  
**Kafka Topic:** `user.profile.created`

**Формат события:**
```json
{
  "event": "user.profile.created",
  "timestamp": "2026-06-14T10:01:00Z",
  "payload": {
    "user_id": 1,
    "keycloak_user_id": "123e4567-e89b-12d3-a456-426614174000",
    "email": "user@example.com",
    "first_name": "John",
    "last_name": "Doe",
    "phone": "+79001234567",
    "verified": false,
    "avatar_url": null,
    "store_name": null,
    "verification_status": "unverified",
    "created_at": "2026-06-14T10:01:00Z"
  },
  "metadata": {
    "source_service": "platform-service",
    "version": "1.0"
  }
}
```

**Auth Service действия:**
- Обновляет данные пользователя в PostgreSQL (дополнительная информация из профиля)

---

## Конфигурация Kafka

### Auth Service (Producer) — application.yml
```yaml
spring:
  kafka:
    bootstrap-servers: ${KAFKA_BOOTSTRAP_SERVERS:localhost:9092}
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
      properties:
        spring.json.trusted.packages: "*"
        acks: all
        retries: 3
        retry.backoff.ms: 1000
```

### Auth Service (Consumer) — application.yml
```yaml
spring:
  kafka:
    bootstrap-servers: ${KAFKA_BOOTSTRAP_SERVERS:localhost:9092}
    consumer:
      group-id: auth-service-sync
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.springframework.kafka.support.serializer.JsonDeserializer
      properties:
        spring.json.trusted.packages: "*"
        auto.offset.reset: earliest
        enable.auto.commit: false
```

---

## Обработка ошибок

### Стратегии

1. **Повторные попытки (Retry):**
   - 3 повторные попытки с экспоненциальной задержкой
   - После 3 неудачных попыток — отправка в Dead Letter Queue (DLQ)

2. **DLQ (Dead Letter Queue):**
   - Topic: `auth.user.registered.dlq`
   - Хранение событий, которые не удалось обработать
   - Ручное восстановление

3. **Идемпотентность:**
   - Проверка существования записи перед вставкой
   - Использование уникальных индексов (keycloak_user_id UNIQUE)

---

## Метрики мониторинга

| Метрика | Описание | Тип |
|---------|----------|-----|
| `kafka_produced_messages_total` | Количество отправленных сообщений | Counter |
| `kafka_consumed_messages_total` | Количество потребленных сообщений | Counter |
| `kafka_failed_messages_total` | Количество неудачных сообщений | Counter |
| `kafka_dlq_messages_total` | Количество сообщений в DLQ | Counter |
| `kafka_lag` | Lag consumer группы | Gauge |

---

## Changelog

| Версия | Дата | Изменения |
|--------|------|-----------|
| 1.0 | 2026-06-14 | Создание единой сводки всех Kafka событий |
| 1.1 | 2026-06-14 | Устранение несоответствия в названиях webhook событий (приведено к snake_case с маленькими буквами) |
| 1.2 | 2026-06-14 | Добавление явного правила отсутствия поля role во все события |
| 1.3 | 2026-06-14 | Добавление ссылки на системное описание архитектурного правила #7 в system-overview.md |
| 1.4 | 2026-06-14 | Добавление явного правила отсутствия поля role во все события и раздел system-overview.md |
| 1.5 | 2026-06-14 | Обновление ссылок на детальную документацию (дедупликация, DLQ, error handling)

---

## Ссылки

- [Детали Kafka событий auth.user.registered](./auth-service-events.md)
- [Консолидация сервисов](../service-consolidation.md)
- [Системный обзор](../system-overview.md)

---

**Контакты:**
- Архитектор: #architecture-team
- Backend Team: #backend-team
