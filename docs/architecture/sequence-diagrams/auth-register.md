# Sequence Diagram: Регистрация пользователя в Keycloak

**Версия документа:** 1.0  
**Дата создания:** 2026-06-14  
**Последнее обновление:** 2026-06-14

---

## Обзор

Диаграмма описывает процесс регистрации нового пользователя в системе AutoDev Marketplace с учетом ключевого принципа: **Keycloak является ЕДИНСТВЕННЫМ источником правды для пользователей и ролей**.

**Детали механизма revoked tokens:** [security/revoked-tokens.md](../security/revoked-tokens.md)  
**Детали RBAC:** [security/rbac.md](../security/rbac.md)  
**Детали JWT структуры:** [security/jwt-structure.md](../security/jwt-structure.md)

---

## Базовая регистрация (User → Keycloak)

```
sequenceDiagram
    participant Client
    participant KeycloakAdminAPI

    Client->>KeycloakAdminAPI: POST /admin/realms/{realm}/users
    Note over Client,KeycloakAdminAPI: Регистрация через Keycloak Admin API
    KeycloakAdminAPI->>KeycloakAdminAPI: Validate request
    KeycloakAdminAPI->>Keycloak: Создание пользователя
    Keycloak->>KeycloakAdminAPI: user_id (keycloak_user_id)
    KeycloakAdminAPI-->>Client: 201 Created
    Note over Client: Пользователь успешно зарегистрирован
```

### Детали запроса

**Request:**
```
POST /admin/realms/{realm}/users
Authorization: Bearer {admin-access-token}
Content-Type: application/json
```

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

**Response:**
```json
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "createdTimestamp": 1655196000000,
  "username": "user@example.com",
  "enabled": true,
  "totp": false,
  "emailVerified": false,
  "firstName": "John",
  "lastName": "Doe",
  "email": "user@example.com",
  "attributes": {}
}
```

---

## Полная регистрация (с синхронизацией)

```
sequenceDiagram
    participant Client
    participant KeycloakAdminAPI
    participant AuthService
    participant PlatformService
    participant Kafka

    Client->>KeycloakAdminAPI: POST /admin/realms/{realm}/users
    Note over Client,KeycloakAdminAPI: Регистрация через Keycloak Admin API или Console
    KeycloakAdminAPI->>KeycloakAdminAPI: Validate request
    KeycloakAdminAPI->>Keycloak: Создание пользователя
    Keycloak->>KeycloakAdminAPI: user_id (keycloak_user_id)
    KeycloakAdminAPI-->>Client: 201 Created
    Note over Client: Пользователь зарегистрирован в Keycloak

    Keycloak->>AuthService: Webhook/Event: user_created
    Note over AuthService: Синхронизация с PostgreSQL
    AuthService->>AuthService: INSERT INTO auth.users
    AuthService->>Kafka: auth.user_registered
    Note over AuthService: Пользователь синхронизирован

    Kafka-->>PlatformService: auth.user_registered
    Note over PlatformService: Создание профиля пользователя
    PlatformService->>PlatformService: INSERT INTO platform_service.users
    PlatformService-->>AuthService: user.profile_created (optional)
    Note over PlatformService: Профиль создан
```

---

## Регистрация через Keycloak Admin Console

```
sequenceDiagram
    participant Admin
    participant KeycloakConsole
    participant AuthService
    participant PlatformService
    participant Kafka

    Admin->>KeycloakConsole: Open /admin/realms/{realm}/users
    KeycloakConsole->>KeycloakConsole: Display registration form

    Admin->>KeycloakConsole: Fill form and submit
    KeycloakConsole->>KeycloakAdminAPI: POST /admin/realms/{realm}/users
    KeycloakAdminAPI->>Keycloak: Создание пользователя
    Keycloak->>KeycloakAdminAPI: user_id (keycloak_user_id)
    KeycloakAdminAPI-->>KeycloakConsole: 201 Created
    KeycloakConsole-->>Admin: Success message

    Keycloak->>AuthService: Webhook/Event: user_created
    AuthService->>AuthService: INSERT INTO auth.users
    AuthService->>Kafka: auth.user_registered

    Kafka-->>PlatformService: auth.user_registered
    PlatformService->>PlatformService: INSERT INTO platform_service.users
```

---

## Регистрация с автоматической отправкой welcome email

```
sequenceDiagram
    participant Client
    participant KeycloakAdminAPI
    participant AuthService
    participant PlatformService
    participant Kafka
    participant CommunicationService

    Client->>KeycloakAdminAPI: POST /admin/realms/{realm}/users
    KeycloakAdminAPI->>Keycloak: Создание пользователя
    KeycloakAdminAPI-->>Client: 201 Created

    Keycloak->>AuthService: Webhook/Event: user_created
    AuthService->>AuthService: INSERT INTO auth.users
    AuthService->>Kafka: auth.user_registered

    Kafka-->>PlatformService: auth.user_registered
    PlatformService->>PlatformService: INSERT INTO platform_service.users
    PlatformService->>Kafka: user.profile.created

    Kafka-->>CommunicationService: user.profile.created
    CommunicationService->>CommunicationService: Build welcome email
    CommunicationService->>SMTP: Send email
    SMTP-->>CommunicationService: 200 OK
    CommunicationService->>Kafka: communication.notification.sent
```

---

## Регистрация с подтверждением email

```
sequenceDiagram
    participant Client
    participant KeycloakAdminAPI
    participant AuthService
    participant PlatformService
    participant Kafka

    Client->>KeycloakAdminAPI: POST /admin/realms/{realm}/users
    Note over Client,KeycloakAdminAPI: emailVerified = false
    KeycloakAdminAPI->>Keycloak: Создание пользователя (emailVerified = false)
    KeycloakAdminAPI-->>Client: 201 Created
    Note over Client: Пользователь зарегистрирован (не подтвержден)

    Keycloak->>AuthService: Webhook/Event: user_created
    AuthService->>AuthService: INSERT INTO auth.users (enabled = false)
    AuthService->>Kafka: auth.user_registered

    Kafka-->>PlatformService: auth.user_registered
    PlatformService->>PlatformService: INSERT INTO platform_service.users (status = pending_verification)

    Note over Client: Email verification required
    Client->>KeycloakAdminAPI: POST /admin/realms/{realm}/users/{id}/verify-email
    KeycloakAdminAPI->>Keycloak: Отправка email verification link
    Keycloak-->>Client: 204 No Content

    Client->>Keycloak: Click verification link
    Keycloak->>KeycloakAdminAPI: PUT /admin/realms/{realm}/users/{id}
    KeycloakAdminAPI->>Keycloak: Update emailVerified = true, enabled = true
    KeycloakAdminAPI-->>Keycloak: 204 No Content

    Keycloak->>AuthService: Webhook/Event: user_updated
    AuthService->>AuthService: UPDATE auth.users SET enabled = true
    AuthService->>Kafka: auth.user_enabled

    Kafka-->>PlatformService: auth.user_enabled
    PlatformService->>PlatformService: UPDATE platform_service.users SET status = active
```

---

## Регистрация с ролью (для администраторов)

```
sequenceDiagram
    participant Admin
    participant KeycloakAdminAPI
    participant AuthService
    participant PlatformService
    participant Kafka

    Admin->>KeycloakAdminAPI: POST /admin/realms/{realm}/users
    Note over Admin,KeycloakAdminAPI: Create user with role mapping
    KeycloakAdminAPI->>Keycloak: Создание пользователя
    KeycloakAdminAPI->>KeycloakAdminAPI: POST /admin/realms/{realm}/users/{id}/role-mappings/realm
    KeycloakAdminAPI->>Keycloak: Assign role (BUYER, SELLER, etc.)
    KeycloakAdminAPI-->>Admin: 201 Created

    Keycloak->>AuthService: Webhook/Event: user_created
    AuthService->>AuthService: INSERT INTO auth.users
    AuthService->>Kafka: auth.user_registered

    Kafka-->>PlatformService: auth.user_registered
    PlatformService->>PlatformService: INSERT INTO platform_service.users (role = {role})
```

---

## Регистрация с дополнительными атрибутами

```
sequenceDiagram
    participant Client
    participant KeycloakAdminAPI
    participant AuthService
    participant PlatformService
    participant Kafka

    Client->>KeycloakAdminAPI: POST /admin/realms/{realm}/users
    Note over Client,KeycloakAdminAPI: With custom attributes
    KeycloakAdminAPI->>Keycloak: Создание пользователя с attributes
    KeycloakAdminAPI-->>Client: 201 Created

    Keycloak->>AuthService: Webhook/Event: user_created
    AuthService->>AuthService: INSERT INTO auth.users
    AuthService->>Kafka: auth.user_registered

    Kafka-->>PlatformService: auth.user_registered
    PlatformService->>PlatformService: INSERT INTO platform_service.users
    PlatformService->>Kafka: user.profile.created
```

---

## Обработка ошибок регистрации

### Ошибка 1: Пользователь уже существует

```
sequenceDiagram
    participant Client
    participant KeycloakAdminAPI

    Client->>KeycloakAdminAPI: POST /admin/realms/{realm}/users
    KeycloakAdminAPI->>Keycloak: Создание пользователя
    Keycloak->>KeycloakAdminAPI: 409 Conflict (User exists)
    KeycloakAdminAPI-->>Client: 409 Conflict
    Note over Client: Error: User with this email already exists
```

**Response:**
```json
{
  "error": "User exists",
  "errorCode": "user_exists"
}
```

### Ошибка 2: Валидация не пройдена

```
sequenceDiagram
    participant Client
    participant KeycloakAdminAPI

    Client->>KeycloakAdminAPI: POST /admin/realms/{realm}/users
    KeycloakAdminAPI->>KeycloakAdminAPI: Validate request
    KeycloakAdminAPI->>KeycloakAdminAPI: Validation failed
    KeycloakAdminAPI-->>Client: 400 Bad Request
    Note over Client: Error: Invalid request data
```

**Response:**
```json
{
  "error": "Invalid request",
  "errorCode": "invalid_request",
  "details": [
    {
      "errorMessage": "Email is invalid",
      "fieldName": "email"
    },
    {
      "errorMessage": "Password is too weak",
      "fieldName": "password"
    }
  ]
}
```

### Ошибка 3: Синхронизация не удалась

```
sequenceDiagram
    participant Keycloak
    participant AuthService

    Keycloak->>AuthService: Webhook/Event: user_created
    AuthService->>AuthService: INSERT INTO auth.users
    AuthService->>AuthService: ERROR: Database connection failed
    AuthService->>AuthService: Retry (3 attempts with exponential backoff)
    AuthService->>AuthService: Fallback: Log error
    Note over AuthService: Алерт в monitoring system
```

---

## Конфигурация Keycloak для webhook

### Шаг 1: Настройка webhooks в Keycloak

Keycloak поддерживает webhooks через события (events). Настроить в `standalone.xml` или через Admin Console.

```xml
<subsystem xmlns="urn:keycloak:events:1.0">
    <listener type="jms">
        <property name="connectionFactory" value="java:/JmsXA"/>
        <property name="destination" value="java:/queue/keycloakEvents"/>
        <property name="transaction" value="true"/>
    </listener>
    <listener type="http">
        <property name="url" value="http://auth-service:8082/api/v1/auth/sync/user-created"/>
        <property name="contentType" value="application/json"/>
        <property name="timeout" value="5000"/>
    </listener>
</subsystem>
```

### Шаг 2: Настройка события user_created

```java
// Keycloak Event Listener Provider
@Provider
public class UserCreatedEventListener implements EventListenerProvider {

    @Override
    public void onEvent(Event event) {
        if (event.getType() == EventType.CREATE_USER) {
            // Send webhook to auth-service
            HttpPost post = new HttpPost("http://auth-service:8082/api/v1/auth/sync/user-created");
            // ...
        }
    }
}
```

---

## Метрики регистрации

### Метрики Keycloak

| Метрика | Описание |
|---------|----------|
| `keycloak_users_created_total` | Количество созданных пользователей |
| `keycloak_users_created_failed_total` | Количество неудачных регистраций |

### Метрики Auth Service

| Метрика | Описание |
|---------|----------|
| `auth_sync_attempts_total` | Количество попыток синхронизации |
| `auth_sync_success_total` | Количество успешных синхронизаций |
| `auth_sync_failed_total` | Количество неудачных синхронизаций |

---

## Часто задаваемые вопросы

### Вопрос 1: Почему нельзя зарегистрировать через Auth Service?

**Ответ:** Это нарушит принцип "Keycloak как единственный источник правды". Auth Service — это обертка над Keycloak для синхронизации, а не альтернативная система регистрации.

### Вопрос 2: Можно ли автоматизировать регистрацию?

**Ответ:** Да, через Keycloak Admin API с service account. Создайте service account в Keycloak и используйте его для автоматической регистрации пользователей.

### Вопрос 3: Как отследить успешность регистрации?

**Ответ:** Используйте метрики и логи:
- Keycloak: `keycloak_users_created_total`
- Auth Service: `auth_sync_success_total`
- Проверка в PostgreSQL: `SELECT * FROM auth.users WHERE email = '...';`

### Вопрос 4: Можно ли отменить регистрацию?

**Ответ:** Да, удалите пользователя через Keycloak Admin API:
```
DELETE /admin/realms/{realm}/users/{user_id}
```

---

## Ссылки

- [Keycloak Admin API Documentation](https://www.keycloak.org/docs-api/15.0/rest-api/index.html)
- [Auth Service Specification](../api-specification/auth-service.yaml)
- [Service Catalog](../service-catalog/auth-service.md)
- [Sequence Diagrams](./README.md)

---

## История изменений

| Версия | Дата | Автор | Описание |
|--------|------|-------|----------|
| 1.0 | 2026-06-14 | Архитектор | Создание документа, описание всех сценариев регистрации |

---

**Контакты:**
- Архитектор: #architecture-team
- Backend Team: #backend-team
