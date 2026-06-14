# Архитектура регистрации пользователей в AutoDev Marketplace

**Версия документа:** 1.2  
**Дата создания:** 2026-06-14  
**Последнее обновление:** 2026-06-14

---

## Обзор

Документ описывает архитектуру регистрации пользователей в AutoDev Marketplace с учетом ключевого принципа: **Keycloak является ЕДИНСТВЕННЫМ источником правды для пользователей и ролей**.

---

## Ключевой принцип архитектуры

> **Auth Service НЕ предоставляет endpoint `POST /api/v1/auth/register`**  
> Регистрация пользователей происходит ТОЛЬКО через Keycloak Admin API или Console.

### Почему?

1. **Keycloak как единственный источник правды**:
   - Роли пользователей хранятся ТОЛЬКО в Keycloak
   - Пользователи создаются ТОЛЬКО в Keycloak
   - PostgreSQL хранит только связи с бизнес-данными

2. **Целостность данных**:
   - Безопасность: Регистрация через один централизованный сервис
   - Единообразие: Все пользователи проходят одинаковую проверку
   - Управление: Единая точка для аудита регистрации

3. **Согласованность**:
   - Auth Service — это обертка над Keycloak, а не альтернативная система
   - PostgreSQL — это синхронизированная копия для производительности

---

## Архитектура регистрации

### Последовательность операций

```
Client → Keycloak Admin API → Auth Service sync → Platform Service profile
```

### Блок-схема

```
┌─────────────┐
│   Client    │
└──────┬──────┘
       │
       │ 1. POST /admin/realms/{realm}/users
       │    (через Keycloak Admin API или Console)
       ▼
┌──────────────┐
│   Keycloak   │
│  (Source of  │
│   Truth)     │
└──────┬───────┘
       │
       │ 2. Webhook/Event: user_created
       ▼
┌──────────────┐
│  Auth Service│
│  (Sync layer)│
└──────┬───────┘
       │
       │ 3. Create record in auth.users (PostgreSQL)
       │ 4. Send Kafka event: auth.user_registered
       ▼
┌──────────────┐
│    Kafka     │
└──────┬───────┘
       │
       │ 5. Consume event: auth.user_registered
       ▼
┌──────────────────┐
│ Platform Service │
│ (Business data)  │
└──────┬───────────┘
       │
       │ 6. Create user profile in platform_service.users
       ▼
┌─────────────┐
│   Client    │
└─────────────┘
```

### Sequence Diagram (корректная)

```
sequenceDiagram
    participant Client
    participant Keycloak
    participant AuthService
    participant PlatformService
    participant Kafka

    Note over Client,PlatformService: Регистрация через Keycloak Admin API или Console
    Client->>Keycloak: POST /admin/realms/{realm}/users
    Keycloak-->>Client: 201 Created (keycloak_user_id)
    Keycloak->>AuthService: Webhook/Event: user_created
    AuthService->>AuthService: Создание записи в auth.users (синхронизация)
    AuthService->>Kafka: auth.user_registered
    Kafka-->>PlatformService: auth.user_registered
    PlatformService->>PlatformService: Создание профиля пользователя
    PlatformService-->>Client: 201 Created (profile created)
```

---

## Детали операций

### Шаг 1: Регистрация в Keycloak

**Кто выполняет:** Admin (через Keycloak Admin Console или API)

**Endpoint Keycloak Admin API:**
```
POST /admin/realms/{realm}/users
```

**Тело запроса:**
```json
{
  "username": "user@example.com",
  "email": "user@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "enabled": true,
  "credentials": [
    {
      "type": "password",
      "value": "Password123!",
      "temporary": false
    }
  ]
}
```

**Ответ:**
```json
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "username": "user@example.com",
  "email": "user@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "enabled": true
}
```

**Важно:**
- Регистрация через Keycloak Admin Console: http://localhost:8090/admin
- Регистрация через Keycloak Admin API: https://www.keycloak.org/docs-api/21.1.1/rest-api/index.html
- Auth Service НЕ предоставляет endpoint для регистрации
- Роли назначаются через Keycloak Admin Console или API (см. `security/rbac.md`)
- Роли хранятся ТОЛЬКО в Keycloak и передаются в JWT токене

### Шаг 2: Синхронизация через Auth Service

**Кто выполняет:** Auth Service (асинхронно через webhook)

**События Keycloak для синхронизации:**

Keycloak отправляет webhook события в Auth Service для всех изменений пользователей:

| Событие Keycloak | Описание | Действие Auth Service |
|------------------|----------|----------------------|
| `user_created` | Создание нового пользователя | Создать запись в `auth.users`, отправить `auth.user_registered` в Kafka |
| `user_updated` | Обновление данных пользователя | Обновить запись в `auth.users`, отправить `auth.user_updated` в Kafka |
| `user_enabled` | Активация пользователя (enabled=true) | Обновить `enabled=true` в `auth.users`, отправить `auth.user.enabled` в Kafka |
| `user_disabled` | Деактивация пользователя (enabled=false) | Обновить `enabled=false` в `auth.users`, отправить `auth.user.disabled` в Kafka |

**Архитектура синхронизации:**

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

**Цепочка для изменения enabled статуса:**

1. Admin меняет статус пользователя в Keycloak Admin Console (enabled=true)
2. Keycloak отправляет webhook в Auth Service: `POST /api/v1/auth/sync` (event: `user_enabled`)
3. Auth Service обновляет PostgreSQL: `UPDATE auth.users SET enabled=true`
4. Auth Service публикует событие в Kafka: `auth.user.enabled`
5. Platform Service получает событие и обновляет профиль пользователя

**Конфигурация webhook в Keycloak 21.1.1:**
1. Зайти в **Keycloak Admin Console**: http://localhost:8090/admin
2. Выбрать realm **autodev** → **Realm settings** → **Events**
3. Включить **Events Enabled** и выбрать **Events Provider**: `http`
4. Указать URL: `http://auth-service:8082/api/v1/auth/sync`
5. Выбрать **Enabled Events**: `user_created`, `user_updated`, `user_enabled`, `user_disabled`
6. Установить **Content Type**: `application/json`
7. Установить **Timeout**: `5000`

**Примечание:** Подробная инструкция по настройке webhook см. в FAQ: "Как настроить webhook в Keycloak?"

### Шаг 3: Создание профиля в Platform Service

**Кто выполняет:** Platform Service (асинхронно через Kafka)

**Событие в Kafka:**
```json
{
  "event": "auth.user_registered",
  "timestamp": "2026-06-14T10:00:00Z",
  "payload": {
    "keycloak_user_id": "123e4567-e89b-12d3-a456-426614174000",
    "email": "user@example.com",
    "created_at": "2026-06-14T10:00:00Z"
  }
}
```

**Platform Service создает профиль:**
```sql
INSERT INTO platform_service.users (keycloak_user_id, email, first_name, last_name, phone, verified, store_name, verification_status)
VALUES ('123e4567-e89b-12d3-a456-426614174000', 'user@example.com', 'John', 'Doe', '+79001234567', false, null, 'unverified');
```

**Важно:** Поле `role` удалено из `platform_service.users` в миграции v0.9.0. Роли хранятся только в Keycloak и передаются в JWT токене.

---

## API endpoints Auth Service (корректные)

| Method | Path | Описание | RBAC |
|--------|------|----------|------|
| `POST` | `/api/v1/auth/login` | Аутентификация пользователя | Публичный |
| `POST` | `/api/v1/auth/refresh` | Обновление токена | Публичный |
| `POST` | `/api/v1/auth/logout` | Выход из системы | BUYER, SELLER, MODERATOR, ADMIN |
| `GET` | `/api/v1/auth/me` | Получение текущего пользователя из кэша Redis | BUYER, SELLER, MODERATOR, ADMIN |
| `POST` | `/api/v1/auth/verify` | Проверка валидности токена | Публичный |
| `GET` | `/api/v1/auth/users/{id}` | Получение пользователя по ID из PostgreSQL (для синхронизации) | ADMIN |
| `GET` | `/api/v1/auth/keys` | Получение публичных ключей Keycloak | Публичный |

**ВАЖНО:** Auth Service НЕ предоставляет endpoints для создания, обновления и удаления пользователей. Все операции с пользователями осуществляются только через Keycloak Admin Console или API.

**ВАЖНО:** Endpoint `GET /api/v1/auth/users/{id}` предоставляет данные из PostgreSQL (таблица `auth.users`) для синхронизации с Keycloak. Это аутентификационные данные (email, enabled). Бизнес-данные профиля (first_name, last_name, phone) предоставляются через `platform-service`.

**ВАЖНО:** Endpoint `GET /api/v1/auth/me` предоставляет данные текущего пользователя из кэша Redis (синхронизировано из PostgreSQL).

**ВАЖНО:** Endpoint `POST /api/v1/auth/register` НЕ ДОЛЖЕН существовать в Auth Service.

**ВАЖНО:** Роли пользователей хранятся только в Keycloak и передаются в JWT токене. Управление ролями осуществляется только через Keycloak Admin Console или API.

**ВАЖНО:** В таблице `platform_service.users` нет поля `role` (удалено в миграции v0.9.0).

---

## Роли пользователей

### Ключевые принципы

1. **Хранение ролей:**
   - Роли хранятся ТОЛЬКО в Keycloak
   - Таблицы `auth.roles`, `auth.permissions`, `auth.role_permissions` НЕ СУЩЕСТВУЮТ

2. **Получение ролей:**
   - Роли выдаются в JWT токене при аутентификации
   - Все сервисы проверяют роли через валидацию JWT токена

3. **Управление ролями:**
   - Только через Keycloak Admin Console: http://localhost:8090/admin
   - Или через Keycloak Admin API: `/admin/realms/{realm}/users/{id}/role-mappings/realm`

### Пример управления ролями через Keycloak Admin API

```
POST /admin/realms/{realm}/users/{user_id}/role-mappings/realm
```

```json
[
  {
    "id": "role-id-buyer",
    "name": "BUYER"
  },
  {
    "id": "role-id-seller",
    "name": "SELLER"
  }
]
```

---

## Конфликты и обработка ошибок

### Сценарий 1: Пользователь уже существует

**Keycloak возвращает:**
```json
{
  "error": "User exists",
  "errorCode": "user_exists"
}
```

**Рекомендация:** Не пытаться зарегистрировать повторно. Использовать login endpoint.

### Сценарий 2: Синхронизация не удалась

**Auth Service логирует ошибку и повторяет попытку:**
- 3 повторные попытки с экспоненциальной задержкой
- После 3 неудачных попыток — алерт в monitoring system

**Platform Service:**
- Событие остается в Kafka (не подтверждается)
- Повторная обработка при следующем запуске

### Сценарий 3: Отсутствующий user_id

**Auth Service проверяет:**
- Если user_id отсутствует — игнорирует событие
- Логирует ошибку для аудита

---

## Мониторинг и логирование

### Метрики

| Метрика | Описание | Тип |
|---------|----------|-----|
| `auth_registration_attempts_total` | Количество попыток регистрации | Counter |
| `auth_registration_failures_total` | Количество неудачных регистраций | Counter |
| `auth_sync_attempts_total` | Количество попыток синхронизации | Counter |
| `auth_sync_failures_total` | Количество неудачных синхронизаций | Counter |

### Логирование

**Auth Service:**
```
INFO  [auth-service] User created in Keycloak: user_id=123e4567-e89b-12d3-a456-426614174000
INFO  [auth-service] Synced user to PostgreSQL: id=1, keycloak_user_id=123e4567-e89b-12d3-a456-426614174000
INFO  [auth-service] Sent Kafka event: event=auth.user_registered, user_id=123e4567-e89b-12d3-a456-426614174000
```

**Platform Service:**
```
INFO  [platform-service] Received Kafka event: event=auth.user_registered, user_id=123e4567-e89b-12d3-a456-426614174000
INFO  [platform-service] Created user profile: id=1, email=user@example.com
```

---

## Безопасность

### Принципы

1. **Контроль доступа:**
   - Регистрация через Keycloak Admin Console — только администраторы
   - Регистрация через Keycloak Admin API — с service account

2. **Валидация:**
   - Проверка email на валидность
   - Проверка пароля на сложность (минимум 8 символов, цифры, спецсимволы)
   - Проверка email на уникальность

3. **Аудит:**
   - Логирование всех операций регистрации
   - Алерты на аномальные паттерны (массовая регистрация с одного IP)

### Пример проверки пароля

```java
@Service
public class PasswordValidator {
    
    private static final int MIN_LENGTH = 8;
    private static final Pattern UPPERCASE = Pattern.compile("[A-Z]");
    private static final Pattern LOWERCASE = Pattern.compile("[a-z]");
    private static final Pattern DIGIT = Pattern.compile("[0-9]");
    private static final Pattern SPECIAL = Pattern.compile("[!@#$%^&*(),.?\":{}|<>]");
    
    public boolean isValid(String password) {
        if (password == null || password.length() < MIN_LENGTH) {
            return false;
        }
        return UPPERCASE.matcher(password).find() &&
               LOWERCASE.matcher(password).find() &&
               DIGIT.matcher(password).find() &&
               SPECIAL.matcher(password).find();
    }
}
```

---

## Migration guide для legacy установок

### Если проект уже имеет endpoint `POST /api/v1/auth/register`

**Шаг 1:** Удалить endpoint из Auth Service

**Шаг 2:** Обновить клиентское приложение

**Шаг 3:** Обучить администраторов использовать Keycloak Admin Console

**Шаг 4:** Настроить миграцию существующих пользователей

```sql
-- Миграция существующих пользователей в Keycloak
INSERT INTO keycloak.users (id, username, email, first_name, last_name, enabled)
SELECT id, email, email, first_name, last_name, enabled
FROM platform_service.users
WHERE keycloak_user_id IS NULL;
```

---

## Часто задаваемые вопросы (FAQ)

### Вопрос 1: Почему нельзя добавить register endpoint в Auth Service?

**Ответ:** Это нарушит архитектурный принцип "Keycloak как единственный источник правды". Это приведет к:
- Дублированию данных
- Несогласованности ролей
- Сложности в управлении
- Безопасным уязвимостям

### Вопрос 2: Можно ли автоматизировать регистрацию?

**Ответ:** Да, через Keycloak Admin API с service account:
- Создать service account в Keycloak
- Дать права на создание пользователей
- Использовать этот account для автоматической регистрации

### Вопрос 3: Как обновить данные пользователя?

**Ответ:** Обновление через Keycloak Admin API:
```
PUT /admin/realms/{realm}/users/{user_id}
```

После этого Auth Service получит событие и обновит PostgreSQL.

### Вопрос 4: Можно ли отключить регистрацию через Admin Console?

**Ответ:** Да, настроить ограничения в Keycloak:
- Роли администраторов без прав на создание пользователей
- Использовать только Admin API с service account

### Вопрос 5: Какие механизмы синхронизации между Keycloak и PostgreSQL?

**Ответ:** Два механизма:

1. **Webhook/Event (рекомендуемый для MVP):**
   - Мгновенная синхронизация при изменении в Keycloak
   - Auth Service принимает webhook и обновляет PostgreSQL
   - Отправляет Kafka события для других сервисов
   - **Конфигурация:** Events Provider в Keycloak 21.1.1 (см. FAQ вопрос 6)

2. **Periodic Sync Job (backup механизм):**
   - Ежедневно или каждые 5 минут запускается job
   - Сравнивает Keycloak и PostgreSQL
   - Синхронизирует различия (для восстановления при сбоях)

**Рекомендация:** Для MVP использовать **Webhook** как основной механизм. Periodic sync job добавить как backup в production.

### Вопрос 6: Как настроить webhook в Keycloak?

**Ответ:** Настройка webhook в Keycloak 21.1.1 осуществляется через **Events Providers**:

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

**Детальное описание событий:**
- `user_created` — создание нового пользователя
- `user_updated` — обновление данных пользователя (email, имя и т.д.)
- `user_enabled` — активация пользователя (изменение статуса enabled=true)
- `user_disabled` — деактивация пользователя (изменение статуса enabled=false)

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
    "enabledEventTypes": ["USER_CREATED"],
    "adminEventsEnabled": "true",
    "adminEventsDetailsEnabled": "true"
  }'

# Настроить HTTP listener
curl -s -X PUT \
  http://localhost:8090/admin/realms/autodev/events/providers/http/config \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "url": "http://auth-service:8082/api/v1/auth/sync/user-created",
    "contentType": "application/json",
    "timeout": "5000"
  }'
```

#### Шаг 3: Проверить настройку

1. Создать пользователя через Keycloak Admin Console
2. Проверить логи Auth Service:
```bash
docker logs -f auth-service
```
3. Должна появиться запись:
```
INFO  [auth-service] Received webhook: event=user_created, user_id=123e4567-e89b-12d3-a456-426614174000
```

---

**Примечание:** В старых версиях Keycloak (до 17.x) использовалась конфигурация через `standalone.xml`:
```xml
<!-- УСТАРЕЛО! Для Keycloak 15.0 и ниже -->
<listener type="http">
    <property name="url" value="http://auth-service:8082/api/v1/auth/sync/user-created"/>
    <property name="contentType" value="application/json"/>
    <property name="timeout" value="5000"/>
</listener>
```

**Для Keycloak 21.1.1 (текущая версия)** используйте **Events Providers** через Admin Console или Admin API.

---

---

## Ссылки

- [Keycloak Admin API Documentation (v21.1.1)](https://www.keycloak.org/docs-api/21.1.1/rest-api/index.html)
- [Keycloak Admin Console (v21.1.1)](http://localhost:8090/admin)
- [Keycloak Events Providers Documentation](https://www.keycloak.org/server/events)
- [Auth Service Specification](../api-specification/auth-service.yaml)
- [Service Catalog](../service-catalog/auth-service.md)
- [Sequence Diagrams](../sequence-diagrams/README.md)
- [Data Model](../data-model.md) - см. миграцию v0.9.0 для удаления поля `role`

---

## История изменений

| Версия |Дата | Автор | Описание |
|--------|------|-------|----------|
| 1.0 | 2026-06-14 | Архитектор | Создание документа, опис архитектуры регистрации |
| 1.1 | 2026-06-14 | Архитектор | Обновление конфигурации webhook для Keycloak 21.1.1 (Events Providers), добавление примеров Admin API, обновление ссылок |
| 1.2 | 2026-06-14 | Архитектор | Устранение несоответствия в названиях webhook событий (приведено к snake_case с маленькими буквами)

---

**Контакты:**
- Архитектор: #architecture-team
- Backend Team: #backend-team
