# Задача 3: Создать DTO классы для аутентификации

## Название задачи
Создать DTO классы для аутентификации и ответов API

## Описание
Создать DTO (Data Transfer Object) классы для обработки входящих и исходящих данных API аутентификации.

**ПОЧЕМУ:** DTO разделяют внутреннюю модель данных (Entities) и внешний API. Это позволяет:
- Не раскрывать внутреннюю структуру сущностей
- Изменять API без изменения сущностей
- Валидировать входные данные
- Упрощает маппинг между сущностями и API

**СООТВЕТСТВИЕ OpenAPI:** См. `docs/architecture/api-specification/auth-service.yaml`

## Критерии выполнения

- [ ] Создан пакет `com.autodev.auth.dto`
- [ ] Создан `LoginRequest` (email, password)
- [ ] Создан `RefreshRequest` (refresh_token)
- [ ] Создан `AuthResponse` (access_token, token_type, expires_in, refresh_token)
- [ ] Создан `UserDto` (id, keycloak_user_id, email, enabled, created_at)
- [ ] Создан `RoleDto` (id, name, description)
- [ ] Создан `CreateRoleRequest` (name, description)
- [ ] Создан `UpdateUserRequest` (email, enabled)
- [ ] Создан `UpdateRoleRequest` (description)
- [ ] Создан `UserPage` (content, total_elements, total_pages, number, size)
- [ ] Создан `Error` (error, message, timestamp)
- [ ] Все DTO соответствуют OpenAPI спецификации

## Примеры структуры
```java
// LoginRequest
public class LoginRequest {
    private String email;
    private String password;
}

// AuthResponse
public class AuthResponse {
    private String accessToken;
    private String tokenType;
    private Long expiresIn;
    private String refreshToken;
}
```

## Ссылки
- [docs/architecture/api-specification/auth-service.yaml](../../docs/architecture/api-specification/auth-service.yaml) - OpenAPI спецификация
- [docs/architecture/glossary.md](../../docs/architecture/glossary.md) - соглашения по DTO

## Приоритет
Высокий

## Метки
backend, dto, auth-service, api
