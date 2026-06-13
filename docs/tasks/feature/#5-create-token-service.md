# Задача 5: Реализовать TokenService для JWT

## Название задачи
Реализовать TokenService для генерации и валидации JWT токенов

## Описание
Создать сервис для управления JWT токенами: генерация access_token и refresh_token, валидация токенов, кэширование в Redis.

**ПОЧЕМУ:** JWT токены - основа аутентификации в системе. Access token (12 часов) используется для авторизации, refresh token (7 дней) для получения нового access token без повторного входа.

## Критерии выполнения

- [ ] Создан пакет `com.autodev.auth.service`
- [ ] Создан класс `TokenService` с `@Service` аннотацией
- [ ] Реализован метод `generateTokens(String keycloakUserId, List<String> roles)` - генерация access и refresh токенов
- [ ] Реализован метод `validateAccessToken(String token)` - валидация access token
- [ ] Реализован метод `validateRefreshToken(String token)` - валидация refresh token
- [ ] Реализован метод `isTokenBlacklisted(String token)` - проверка черного списка
- [ ] Реализован метод `addToBlacklist(String token)` - добавление в черный список (logout)
- [ ] Настроено кэширование токенов в Redis
- [ ] Используется Spring Security JWT
- [ ] Access token TTL: 12 часов
- [ ] Refresh token TTL: 7 дней
- [ ] Используется RS256 или HS256 алгоритм подписи

## Пример структуры токена
```java
// Access Token (12 часов)
{
  "sub": "keycloak_user_id",
  "email": "user@example.com",
  "roles": ["BUYER", "SELLER"],
  "exp": timestamp,
  "iat": timestamp
}

// Refresh Token (7 дней)
{
  "sub": "keycloak_user_id",
  "type": "refresh",
  "exp": timestamp,
  "iat": timestamp
}
```

## Ссылки
- [docs/architecture/security-strategy.md](../../docs/architecture/security-strategy.md) - стратегия безопасности
- [docs/architecture/glossary.md](../../docs/architecture/glossary.md) - JWT определение

## Приоритет
Высокий

## Метки
backend, security, jwt, auth-service
