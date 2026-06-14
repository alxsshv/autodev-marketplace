# Kafka события Auth Service

**Версия документа:** 1.4  
**Дата создания:** 2026-06-14  
**Последнее обновление:** 2026-06-14

---

## Обзор

Документ описывает Kafka события, генерируемые и потребляемые Auth Service для синхронизации пользователей между Keycloak и PostgreSQL.

**Важно:** All события синхронизации пользователей между Keycloak и PostgreSQL настроены через **Keycloak Events Provider**. Keycloak отправляет webhook события со следующими названиями: `user_created`, `user_updated`, `user_enabled`, `user_disabled` (snake_case, строчные буквы). Детальная настройка и описание процессов см. в `security/user-registration-architecture.md`.

---

## Обновления в версии 1.4

### Добавлены разделы:

1. **Дедупликация событий** — стратегия предотвращения обработки дубликатов webhook событий от Keycloak
2. **Dead Letter Queue (DLQ)** — полное описание стратегии обработки невалидных событий
3. **Стратегия обработки ошибок синхронизации** — сценарии для PostgreSQL недоступности, конфликтов ключей, невалидных данных

---

## Общая архитектура

```
┌──────────────┐
│   Keycloak   │
│  (Source of  │
│   Truth)     │
└──────┬───────┘
       │
       │ Webhook (user_created, user_updated, user_enabled, user_disabled)
       ▼
┌──────────────┐
│  Auth Service│
│              │
│  - Принимает │
│  - Обновляет │
│  - Отправляет│
└──────┬───────┘
       │
       │ Kafka Events (auth.user.*)
       ▼
┌──────────────┐
│    Kafka     │
└──────┬───────┘
       │
       │ Events
       ▼
┌──────────────────┐
│ Platform Service │
│ (Потребитель)    │
└──────────────────┘
```

**Примечание:** Keycloak отправляет webhook события в Auth Service для всех изменений пользователей. Auth Service обновляет PostgreSQL и публикует Kafka события для других сервисов. Подробности о настройке webhook см. в `security/user-registration-architecture.md`.

---

## События, генерируемые Auth Service

### 1. auth.user.registered

**Источник:** Auth Service  
**Когда:** После создания пользователя в Keycloak и синхронизации в PostgreSQL

**Цепочка операций:**
1. Admin создает пользователя через Keycloak Admin Console или API
2. Keycloak создает пользователя и отправляет webhook в Auth Service (event: `user_created`)
3. Auth Service синхронизирует PostgreSQL (INSERT INTO auth.users)
4. Auth Service публикует событие в Kafka: `auth.user.registered`

**Формат webhook от Keycloak:**
```json
{
  "event": "user_created",
  "timestamp": "2026-06-14T10:00:00Z",
  "user": {
    "id": "123e4567-e89b-12d3-a456-426614174000",
    "username": "user@example.com",
    "email": "user@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "enabled": true
  }
}
```

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

### ⚠️ ВАЖНО: Правило отсутствия поля role

**Поле `role` НЕ ДОЛЖНО и НЕ БУДЕТ присутствовать в payload любого Kafka события!**

**Почему?**
- Роли пользователей хранятся ТОЛЬКО в Keycloak
- При аутентификации JWT токен содержит список ролей
- В PostgreSQL нет таблиц `auth.roles`, `auth.permissions`, `auth.role_permissions`
- В `platform_service.user_profiles` нет поля `role` (удалено в миграции v0.9.0)

**Что должно содержаться в payload событий:**
- ✅ Только аутентификационные данные (email, enabled status, keycloak_user_id, created_at)
- ✅ Только бизнес-данные профиля (first_name, last_name, phone, verified, avatar_url и т.д.)

**Что НЕ должно содержаться в payload событий:**
- ❌ Поле `role` или `roles` — оно никогда не передается между сервисами через Kafka
- ❌ Структура `realm_access.roles` — это только для JWT токенов Keycloak

**Потребители:**
- **Platform Service** — создание профиля пользователя в `platform_service.users`
- **Communication Service** — отправка welcome email

**Ключ Kafka topic:** `auth.user.registered`

### 2. auth.user.updated

**Источник:** Auth Service  
**Когда:** После обновления аутентификационных данных пользователя в Keycloak и синхронизации в PostgreSQL

**Цепочка операций:**
1. Admin обновляет данные через Keycloak Admin Console/API
2. Keycloak отправляет webhook в Auth Service (event: `user_updated`)
3. Auth Service синхронизирует PostgreSQL (UPDATE auth.users)
4. Auth Service публикует событие в Kafka: `auth.user.updated`

**Формат webhook от Keycloak:**
```json
{
  "event": "user_updated",
  "timestamp": "2026-06-14T10:05:00Z",
  "user": {
    "id": "123e4567-e89b-12d3-a456-426614174000",
    "username": "user@example.com",
    "email": "user.new@example.com",
    "enabled": false
  }
}
```

**Формат события:****
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

### ⚠️ ВАЖНО: Правило отсутствия поля role

**Поле `role` НЕ ДОЛЖНО и НЕ БУДЕТ присутствовать в payload любого Kafka события!**

**Почему?**
- Роли пользователей хранятся ТОЛЬКО в Keycloak
- При аутентификации JWT токен содержит список ролей
- В PostgreSQL нет таблиц `auth.roles`, `auth.permissions`, `auth.role_permissions`
- В `platform_service.user_profiles` нет поля `role` (удалено в миграции v0.9.0)

**Что должно содержаться в payload событий:**
- ✅ Только аутентификационные данные (email, enabled status, keycloak_user_id, created_at)
- ✅ Только бизнес-данные профиля (first_name, last_name, phone, verified, avatar_url и т.д.)

**Что НЕ должно содержаться в payload событий:**
- ❌ Поле `role` или `roles` — оно никогда не передается между сервисами через Kafka
- ❌ Структура `realm_access.roles` — это только для JWT токенов Keycloak

**Потребители:**
- **Platform Service** — обновление профиля пользователя
- **Communication Service** — уведомление об изменении данных

**Ключ Kafka topic:** `auth.user.updated`

### 3. auth.user.deleted

**Источник:** Auth Service  
**Когда:** После удаления пользователя из Keycloak и PostgreSQL

**Цепочка операций:**
1. Admin удаляет пользователя через Keycloak Admin Console/API
2. Keycloak удаляет пользователя и отправляет webhook в Auth Service (event: `user_deleted`)
3. Auth Service удаляет из PostgreSQL
4. Auth Service публикует событие в Kafka: `auth.user.deleted`

**Формат webhook от Keycloak:**
```json
{
  "event": "user_deleted",
  "timestamp": "2026-06-14T10:10:00Z",
  "user": {
    "id": "123e4567-e89b-12d3-a456-426614174000"
  }
}
```

**Формат события:****
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

### ⚠️ ВАЖНО: Правило отсутствия поля role

**Поле `role` НЕ ДОЛЖНО и НЕ БУДЕТ присутствовать в payload любого Kafka события!**

**Почему?**
- Роли пользователей хранятся ТОЛЬКО в Keycloak
- При аутентификации JWT токен содержит список ролей
- В PostgreSQL нет таблиц `auth.roles`, `auth.permissions`, `auth.role_permissions`
- В `platform_service.user_profiles` нет поля `role` (удалено в миграции v0.9.0)

**Что должно содержаться в payload событий:**
- ✅ Только аутентификационные данные (email, enabled status, keycloak_user_id, created_at)
- ✅ Только бизнес-данные профиля (first_name, last_name, phone, verified, avatar_url и т.д.)

**Что НЕ должно содержаться в payload событий:**
- ❌ Поле `role` или `roles` — оно никогда не передается между сервисами через Kafka
- ❌ Структура `realm_access.roles` — это только для JWT токенов Keycloak

**Потребители:**
- **Platform Service** — удаление профиля пользователя
- **Communication Service** — остановка уведомлений

**Ключ Kafka topic:** `auth.user.deleted`

### 4. auth.user.enabled

**Источник:** Auth Service  
**Когда:** После включения пользователя (enabled = true) через Keycloak Admin Console/API

**Цепочка операций:**
1. Admin меняет статус в Keycloak
2. Keycloak отправляет webhook в Auth Service (event: `user_enabled`)
3. Auth Service обновляет PostgreSQL (UPDATE auth.users SET enabled = true)
4. Auth Service публикует событие в Kafka: `auth.user.enabled`

**Формат webhook от Keycloak:**
```json
{
  "event": "user_enabled",
  "timestamp": "2026-06-14T10:15:00Z",
  "user": {
    "id": "123e4567-e89b-12d3-a456-426614174000",
    "enabled": true
  }
}
```

**Формат события:****
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

### ⚠️ ВАЖНО: Правило отсутствия поля role

**Поле `role` НЕ ДОЛЖНО и НЕ БУДЕТ присутствовать в payload любого Kafka события!**

**Почему?**
- Роли пользователей хранятся ТОЛЬКО в Keycloak
- При аутентификации JWT токен содержит список ролей
- В PostgreSQL нет таблиц `auth.roles`, `auth.permissions`, `auth.role_permissions`
- В `platform_service.user_profiles` нет поля `role` (удалено в миграции v0.9.0)

**Что должно содержаться в payload событий:**
- ✅ Только аутентификационные данные (email, enabled status, keycloak_user_id, created_at)
- ✅ Только бизнес-данные профиля (first_name, last_name, phone, verified, avatar_url и т.д.)

**Что НЕ должно содержаться в payload событий:**
- ❌ Поле `role` или `roles` — оно никогда не передается между сервисами через Kafka
- ❌ Структура `realm_access.roles` — это только для JWT токенов Keycloak

**Потребители:**
- **Platform Service** — разблокировка профиля
- **Communication Service** — уведомление о восстановлении доступа

**Ключ Kafka topic:** `auth.user.enabled`

### 5. auth.user.disabled

**Источник:** Auth Service  
**Когда:** После отключения пользователя (enabled = false) через Keycloak Admin Console/API

**Цепочка операций:**
1. Admin меняет статус в Keycloak
2. Keycloak отправляет webhook в Auth Service (event: `user_disabled`)
3. Auth Service обновляет PostgreSQL (UPDATE auth.users SET enabled = false)
4. Auth Service публикует событие в Kafka: `auth.user.disabled`

**Формат webhook от Keycloak:**
```json
{
  "event": "user_disabled",
  "timestamp": "2026-06-14T10:20:00Z",
  "user": {
    "id": "123e4567-e89b-12d3-a456-426614174000",
    "enabled": false,
    "disabledBy": "admin@example.com",
    "reason": "Policy violation"
  }
}
```

**Формат события:****
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

### ⚠️ ВАЖНО: Правило отсутствия поля role

**Поле `role` НЕ ДОЛЖНО и НЕ БУДЕТ присутствовать в payload любого Kafka события!**

**Почему?**
- Роли пользователей хранятся ТОЛЬКО в Keycloak
- При аутентификации JWT токен содержит список ролей
- В PostgreSQL нет таблиц `auth.roles`, `auth.permissions`, `auth.role_permissions`
- В `platform_service.user_profiles` нет поля `role` (удалено в миграции v0.9.0)

**Что должно содержаться в payload событий:**
- ✅ Только аутентификационные данные (email, enabled status, keycloak_user_id, created_at)
- ✅ Только бизнес-данные профиля (first_name, last_name, phone, verified, avatar_url и т.д.)

**Что НЕ должно содержаться в payload событий:**
- ❌ Поле `role` или `roles` — оно никогда не передается между сервисами через Kafka
- ❌ Структура `realm_access.roles` — это только для JWT токенов Keycloak

**Потребители:**
- **Platform Service** — блокировка профиля
- **Communication Service** — уведомление о блокировке

**Ключ Kafka topic:** `auth.user.disabled`

---

## События, потребляемые Auth Service

### 1. user.profile_created (из Platform Service)

**Источник:** Platform Service  
**Когда:** После создания профиля пользователя (для служебных учетных записей)

**Формат события:**
```json
{
  "event": "user.profile.created",
  "timestamp": "2026-06-14T10:01:00Z",
  "payload": {
    "keycloak_user_id": "123e4567-e89b-12d3-a456-426614174000",
    "email": "user@example.com",
    "first_name": "John",
    "last_name": "Doe",
    "phone": "+79001234567"
  },
  "metadata": {
    "source_service": "platform-service",
    "version": "1.0"
  }
}
```

**Auth Service действия:**
- Обновляет данные пользователя в PostgreSQL (дополнительная информация из профиля)
- **ВАЖНО:** Роли не передаются в PostgreSQL - они хранятся только в Keycloak

**Ключ Kafka topic:** `user.profile.created`

---

## Конфигурация Kafka

### Auth Service (Producer)

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

### Auth Service (Consumer)

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

## Обработка событий в Auth Service

### Пример кода (Spring Boot)

```java
@Service
public class UserService {

    @KafkaListener(topics = "user.profile.created", groupId = "auth-service-sync")
    @Transactional
    public void handleUserProfileCreated(ConsumerRecord<String, UserProfileCreatedEvent> record) {
        UserProfileCreatedEvent event = record.value();
        
        logger.info("Received event: {}", event);
        
        // Find user by keycloak_user_id
        User user = userRepository.findByKeycloakUserId(event.getPayload().getKeycloakUserId());
        if (user == null) {
            logger.error("User not found: {}", event.getPayload().getKeycloakUserId());
            return;
        }
        
        // Update user data
        user.setFirstName(event.getPayload().getFirstName());
        user.setLastName(event.getPayload().getLastName());
        user.setPhone(event.getPayload().getPhone());
        
        userRepository.save(user);
        
        logger.info("User updated: {}", user.getId());
    }
}
```

### Пример кода (Producer)

```java
@Service
public class UserSyncService {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    public void notifyUserRegistered(User user) {
        UserRegisteredEvent event = new UserRegisteredEvent(
            user.getId(),
            user.getKeycloakUserId(),
            user.getEmail(),
            user.isEnabled(),
            user.getCreatedAt()
        );

        kafkaTemplate.send("auth.user.registered", event);
        logger.info("Sent event: auth.user.registered for user {}", user.getId());
    }

    public void notifyUserUpdated(User user) {
        UserUpdatedEvent event = new UserUpdatedEvent(
            user.getId(),
            user.getKeycloakUserId(),
            user.getEmail(),
            user.isEnabled(),
            user.getCreatedAt()
        );

        kafkaTemplate.send("auth.user.updated", event);
        logger.info("Sent event: auth.user.updated for user {}", user.getId());
    }

    public void notifyUserEnabled(Long userId, String keycloakUserId) {
        UserEnabledEvent event = new UserEnabledEvent(
            userId,
            keycloakUserId,
            true,
            LocalDateTime.now()
        );

        kafkaTemplate.send("auth.user.enabled", event);
        logger.info("Sent event: auth.user.enabled for user {}", userId);
    }

    public void notifyUserDisabled(Long userId, String keycloakUserId, String reason, String disabledBy) {
        UserDisabledEvent event = new UserDisabledEvent(
            userId,
            keycloakUserId,
            true,
            LocalDateTime.now(),
            disabledBy,
            reason
        );

        kafkaTemplate.send("auth.user.disabled", event);
        logger.info("Sent event: auth.user.disabled for user {}", userId);
    }

    public void notifyUserDeleted(Long userId, String keycloakUserId) {
        UserDeletedEvent event = new UserDeletedEvent(
            userId,
            keycloakUserId,
            LocalDateTime.now()
        );

        kafkaTemplate.send("auth.user.deleted", event);
        logger.info("Sent event: auth.user.deleted for user {}", userId);
    }
}
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
   - Использование уникальных индексов

### Пример кода (Retry + DLQ)

```java
@Service
public class EventErrorHandler {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Bean
    public DefaultErrorHandler errorHandler() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        configs.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configs.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        configs.put(JsonDeserializer.TRUSTED_PACKAGES, "*");

        DefaultErrorHandler handler = new DefaultErrorHandler(
            (record, exception) -> {
                logger.error("Failed to process event: {}", record, exception);
                // Send to DLQ
                kafkaTemplate.send("auth.user.registered.dlq", record);
            },
            new FixedBackOff(1000L, 3)
        );

        return handler;
    }
}
```

---

## Дедупликация событий

### Проблема

Keycloak может отправлять дубликаты webhook событий при повторных попытках (например, при таймаутах или сетевых ошибках).

### Решение

1. Использовать уникальный `event_id` из события Keycloak
2. Хранить обработанные `event_id` в Redis с TTL = 1 час
3. Проверка перед обработкой:

```java
@Service
public class WebhookEventHandler {

    @Autowired
    private RedisTemplate<String, Boolean> redisTemplate;

    @PostMapping("/api/v1/auth/sync")
    public ResponseEntity<Void> handleWebhook(@RequestBody KeycloakWebhookEvent event) {
        String eventId = event.getEventId();
        if (eventId == null || eventId.isEmpty()) {
            logger.warn("Webhook event without event_id received");
            return ResponseEntity.badRequest().build();
        }

        String redisKey = "webhook:processed:" + eventId;
        Boolean alreadyProcessed = redisTemplate.opsForValue().get(redisKey);

        if (alreadyProcessed != null && Boolean.TRUE.equals(alreadyProcessed)) {
            logger.info("Duplicate webhook event skipped: {}", eventId);
            return ResponseEntity.ok().build();
        }

        // Обработка события...
        processWebhookEvent(event);

        // Пометить как обработанное
        redisTemplate.opsForValue().set(redisKey, true, 1, TimeUnit.HOURS);

        return ResponseEntity.ok().build();
    }

    private void processWebhookEvent(KeycloakWebhookEvent event) {
        // Логика обработки webhook
    }
}
```

### Пример webhook от Keycloak с event_id

```json
{
  "event_id": "a1b2c3d4-e5f6-7890-g1h2-i3j4k5l6m7n8",
  "event_type": "user_created",
  "realm_id": "autodev",
  "client_id": "admin-cli",
  "timestamp": 1623667200000,
  "user_id": "123e4567-e89b-12d3-a456-426614174000",
  "ip_address": "127.0.0.1",
  "details": {
    "username": "user@example.com",
    "email": "user@example.com",
    "firstName": "John",
    "lastName": "Doe"
  }
}
```

**Примечание:** Если Keycloak не предоставляет `event_id`, использовать комбинацию `user_id + timestamp + event_type` как уникальный идентификатор.

---

## Dead Letter Queue (DLQ)

### Стратегия DLQ для MVP

| Topic | Описание |
|-------|----------|
| `auth.user.registered.dlq` | Невалидные события регистрации |
| `auth.user.updated.dlq` | Невалидные события обновления |
| `auth.user.deleted.dlq` | Невалидные события удаления |
| `auth.user.enabled.dlq` | Невалидные события включения |
| `auth.user.disabled.dlq` | Невалидные события отключения |

### Конфигурация Spring Kafka

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
        isolation.level: read_committed
    listener:
      missing-topics-fatal: false
```

### Обработка ошибок с DLQ

```java
@Service
public class KafkaConsumerConfig {

    @Bean
    public DefaultErrorHandler errorHandler(KafkaTemplate<String, Object> kafkaTemplate) {
        Map<String, Object> configs = new HashMap<>();
        configs.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        configs.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configs.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        configs.put(JsonDeserializer.TRUSTED_PACKAGES, "*");

        DefaultErrorHandler handler = new DefaultErrorHandler(
            (record, exception) -> {
                logger.error("Failed to process event: {}", record, exception);

                String dlqTopic = getDlqTopic(record.topic());
                kafkaTemplate.send(dlqTopic, record);

                logger.info("Sent to DLQ: {}", dlqTopic);
            },
            new FixedBackOff(1000L, 3)
        );

        return handler;
    }

    private String getDlqTopic(String originalTopic) {
        return originalTopic + ".dlq";
    }
}
```

### Мониторинг DLQ

| Метрика | Описание | Целевое значение |
|---------|----------|-----------------|
| `kafka_dlq_messages_total` | Количество сообщений в DLQ | < 100 |
| `kafka_dlq_age_hours` | Средний возраст сообщений в DLQ | < 24 часа |
| `kafka_dlq_processing_errors` | Количество ошибок при обработке DLQ | 0 |

**Алерты:**
- `DLQ message count > 100` — Алерт в Slack
- `DLQ message age > 24 hours` — Алерт в Slack + Email

---

## Стратегия обработки ошибок синхронизации

### Сценарий 1: PostgreSQL недоступен

**Описание:** PostgreSQL недоступен при попытке синхронизации.

**Стратегия:**

1. Повторная попытка через 1 секунду (第一回)
2. Повторная попытка через 5 секунд (第二回)
3. Повторная попытка через 15 секунд (第三回)
4. Если не удалось — логирование в Loki и алерт в Slack
5. Повторная обработка при следующем запуске (Kafka auto-offset-reset = earliest)

**Пример кода:**

```java
@Service
public class PostgresSyncService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Retryable(
        value = {DataAccessException.class, SQLException.class},
        maxAttempts = 4,
        backoff = new BackoffSchedule(1000, 5000, 15000)
    )
    public void syncUserToPostgres(User user) {
        try {
            jdbcTemplate.update(
                "INSERT INTO auth.users (keycloak_user_id, email, enabled, created_at) VALUES (?, ?, ?, ?)"
                    + " ON CONFLICT (keycloak_user_id) DO UPDATE SET email = EXCLUDED.email, enabled = EXCLUDED.enabled",
                user.getKeycloakUserId(),
                user.getEmail(),
                user.isEnabled(),
                user.getCreatedAt()
            );
        } catch (DataAccessException e) {
            logger.error("Failed to sync user to PostgreSQL: {}", user.getKeycloakUserId(), e);
            // Алерт в Slack через Webhook
            slackAlertService.sendAlert("PostgreSQL sync failed: " + e.getMessage());
            throw e;
        }
    }
}
```

### Сценарий 2: Конфликт ключа (duplicate key)

**Описание:** Попытка вставить пользователя с уже существующим `keycloak_user_id`.

**Стратегия:**

1. Логирование ошибки
2. Проверка, существует ли запись в PostgreSQL
3. Если существует — логирование "skipped (already synced)"
4. Если не существует — повторная попытка (возможно, это вставка после удаления)

**Пример кода:**

```java
@Service
public class PostgresSyncService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Retryable(
        value = {DataAccessException.class, SQLException.class},
        maxAttempts = 2
    )
    public void syncUserToPostgres(User user) {
        try {
            int updated = jdbcTemplate.update(
                "INSERT INTO auth.users (keycloak_user_id, email, enabled, created_at) VALUES (?, ?, ?, ?)"
                    + " ON CONFLICT (keycloak_user_id) DO UPDATE SET email = EXCLUDED.email, enabled = EXCLUDED.enabled",
                user.getKeycloakUserId(),
                user.getEmail(),
                user.isEnabled(),
                user.getCreatedAt()
            );

            if (updated == 0) {
                logger.info("User already synced, skipped: {}", user.getKeycloakUserId());
            }
        } catch (DataAccessException e) {
            logger.error("Failed to sync user to PostgreSQL: {}", user.getKeycloakUserId(), e);

            // Проверка существования
            Boolean exists = jdbcTemplate.queryForObject(
                "SELECT EXISTS(SELECT 1 FROM auth.users WHERE keycloak_user_id = ?)",
                Boolean.class,
                user.getKeycloakUserId()
            );

            if (Boolean.TRUE.equals(exists)) {
                logger.info("User already exists, skipped: {}", user.getKeycloakUserId());
            } else {
                throw e;
            }
        }
    }
}
```

### Сценарий 3: Невалидные данные от Keycloak

**Описание:** Keycloak отправляет webhook с некорректными данными (например, отсутствует `email`, `keycloak_user_id`).

**Стратегия:**

1. Логирование с полным payload
2. Отправка в DLQ (не в основную очередь!)
3. Алерт в Slack с link to Sentry
4. Ручное вмешательство для анализа

**Пример кода:**

```java
@RestController
public class WebhookController {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @PostMapping("/api/v1/auth/sync")
    public ResponseEntity<Void> handleWebhook(@RequestBody Map<String, Object> event) {
        try {
            // Валидация обязательных полей
            validateWebhookEvent(event);

            // Обработка события
            processWebhookEvent(event);

            return ResponseEntity.ok().build();
        } catch (ValidationException e) {
            logger.error("Invalid webhook event: {}", event, e);

            // Отправка в DLQ
            kafkaTemplate.send("webhook.dlq", event);

            // Алерт в Slack
            slackAlertService.sendAlert("Invalid webhook event: " + e.getMessage());

            return ResponseEntity.badRequest().build();
        }
    }

    private void validateWebhookEvent(Map<String, Object> event) {
        String eventId = (String) event.get("event_id");
        String eventType = (String) event.get("event_type");
        Map<String, Object> details = (Map<String, Object>) event.get("details");

        if (eventId == null || eventId.isEmpty()) {
            throw new ValidationException("Missing event_id");
        }

        if (eventType == null || eventType.isEmpty()) {
            throw new ValidationException("Missing event_type");
        }

        if (details == null) {
            throw new ValidationException("Missing details");
        }

        String email = (String) details.get("email");
        if (email == null || email.isEmpty()) {
            throw new ValidationException("Missing email in details");
        }
    }
}
```

---

## Мониторинг

### Метрики

| Метрика | Описание | Тип |
|---------|----------|-----|
| `kafka_produced_messages_total` | Количество отправленных сообщений | Counter |
| `kafka_consumed_messages_total` | Количество потребленных сообщений | Counter |
| `kafka_failed_messages_total` | Количество неудачных сообщений | Counter |
| `kafka_dlq_messages_total` | Количество сообщений в DLQ | Counter |
| `kafka_lag` | Lag consumer группы | Gauge |

### Алерты

| Алерт | Условие | Действие |
|-------|---------|----------|
| `HighKafkaLag` | Lag > 1000 | Алерт в Slack |
| `HighFailedMessages` | Failed > 10 за 5 мин | Алерт в Slack + Email |
| `DLQメッセージ増加` | DLQ messages > 100 | Алерт в Slack + Email |

---

## Порядок событий

### Идеальный сценарий

```
1. Keycloak создает пользователя
2. Keycloak отправляет webhook/event в Auth Service
3. Auth Service создает запись в PostgreSQL
4. Auth Service отправляет событие auth.user_registered
5. Platform Service получает событие и создает профиль
6. Platform Service отправляет событие user.profile_created
7. Auth Service получает событие и обновляет данные
```

### Сценарий с задержкой

```
1. Keycloak создает пользователя
2. Keycloak отправляет webhook/event в Auth Service ( задержка)
3. Platform Service получает событие auth.user_registered (запаздывает)
4. Platform Service пытается создать профиль, но user еще не найден
5. Platform Service логирует ошибку и повторяет попытку
6. Auth Service получает webhook/event
7. Auth Service создает запись в PostgreSQL
8. Platform Service повторно получает событие и создает профиль
```

**Решение:**
- Platform Service должен проверять существование пользователя перед созданием профиля
- Если пользователь не найден — сохранять событие для повторной обработки

---

## Безопасность

### ACL (Access Control Lists)

```bash
# Создание ACL для Auth Service
kafka-acls.sh --authorizer-properties zookeeper.connect=localhost:2181 \
  --add --allow-principal User:auth-service \
  --operation READ --topic auth.user.registered \
  --operation WRITE --topic auth.user.registered
```

### Шифрование

- Kafka SSL для шифрования данных в transit
- TLS 1.3 для соединений

---

## Часто задаваемые вопросы

### Вопрос 1: Почему использовать Kafka вместо прямого вызова API?

**Ответ:**
- Асинхронность — не блокирует основной поток
- Надежность — события не теряются
- Масштабируемость — легко добавить новых потребителей
- Decoupling — сервисы не зависят друг от друга

### Вопрос 2: Можно ли потерять события?

**Ответ:**
- No, если настроить `acks=all` и репликацию
- Использовать `enable.auto.commit=false` для контроля
- DLQ для событий, которые не удалось обработать

### Вопрос 3: Как обеспечить идемпотентность?

**Ответ:**
- Уникальные индексы в PostgreSQL
- Проверка существования записи перед вставкой
- Использование `UPSERT` (INSERT ... ON CONFLICT DO NOTHING)

### Вопрос 4: Как настроить события в Keycloak для синхронизации статуса enabled?

**Ответ:**

#### Шаг 1: Включить HTTP Events Provider

1. Зайти в **Keycloak Admin Console**: http://localhost:8090/admin
2. Выбрать realm **autodev**
3. Перейти в **Realm settings** → **Events**
4. Включить **Events Enabled**
5. Выбрать **Events Provider**: `http`

#### Шаг 2: Настроить HTTP Events Provider

В разделе **HTTP Events Provider** заполнить следующие поля:

| Поле | Значение | Описание |
|------|----------|----------|
| **URL** | `http://auth-service:8082/api/v1/auth/sync` | Endpoint Auth Service для получения webhook |
| **Content Type** | `application/json` | Формат данных |
| **Timeout** | `5000` | Таймаут в миллисекундах |
| **Enabled Events** | `user_created`, `user_updated`, `user_enabled`, `user_disabled` | События, которые нужно отправлять (в формате snake_case, строчные буквы) |

**Важно:** Имена событий должны быть в формате snake_case с маленькими буквами, как указано в таблице. Это соответствует формату, который отправляет Keycloak Events Provider.

#### Альтернатива: Программная настройка через Admin API

```bash
# Получить access token для Admin API
TOKEN=$(curl -s -X POST \
  http://localhost:8090/realms/master/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "client_id=admin-cli" \
  -d "grant_type=password" \
  -d "username=admin" \
  -d "password=admin" | jq -r '.access_token')

# Настроить HTTP Events Provider для realm autodev
curl -s -X PUT \
  http://localhost:8090/admin/realms/autodev/events/config \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "eventsEnabled": "true",
    "eventsListeners": ["http"],
    "enabledEventTypes": ["user_created", "user_updated", "user_enabled", "user_disabled"],
    "adminEventsEnabled": "true",
    "adminEventsDetailsEnabled": "true"
  }'

# Настроить HTTP listener
curl -s -X PUT \
  http://localhost:8090/admin/realms/autodev/events/providers/http/config \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "url": "http://auth-service:8082/api/v1/auth/sync",
    "contentType": "application/json",
    "timeout": "5000"
  }'
```

#### Шаг 3: Проверить настройку

1. Изменить статус пользователя (включить/отключить) через Keycloak Admin Console
2. Проверить логи Auth Service:
```bash
docker logs -f auth-service
```
3. Должны появиться записи:
```
INFO  [auth-service] Received webhook: event=user_enabled, user_id=123e4567-e89b-12d3-a456-426614174000
INFO  [auth-service] Synced enabled status in PostgreSQL: user_id=1, enabled=true
INFO  [auth-service] Sent Kafka event: event=auth.user.enabled, user_id=1
```

---

## Ссылки

- [Kafka Documentation](https://kafka.apache.org/documentation)
- [Spring Kafka Documentation](https://docs.spring.io/spring-kafka/docs/current/reference/html)
- [Keycloak Admin API](https://www.keycloak.org/docs-api/15.0/rest-api/index.html)
- [Auth Service Specification](../api-specification/auth-service.yaml)

---

## История изменений

| Версия | Дата | Автор | Описание |
|--------|------|-------|----------|
| 1.0 | 2026-06-14 | Архитектор | Создание документа, описание всех событий |
| 1.1 | 2026-06-14 | Архитектор | Обновление описания событий с указанием типов webhook от Keycloak, добавление примеров кода для user_enabled/user_disabled, FAQ про настройку событий в Keycloak |
| 1.2 | 2026-06-14 | Архитектор | Устранение несоответствия в названиях webhook событий (приведено к snake_case с маленькими буквами) |
| 1.3 | 2026-06-14 | Архитектор | Добавление явного правила отсутствия поля role во все события и раздел system-overview.md |
| 1.4 | 2026-06-14 | Архитектор | Добавление разделов: дедупликация событий, DLQ, стратегия обработки ошибок синхронизации

---

**Контакты:**
- Архитектор: #architecture-team
- Backend Team: #backend-team
