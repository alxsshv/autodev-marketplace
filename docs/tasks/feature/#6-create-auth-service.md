# Задача 6: Реализовать AuthService с бизнес-логикой

## Название задачи
Реализовать AuthService с бизнес-логикой аутентификации

## Описание
Создать сервис с бизнес-логикой для основных операций аутентификации: login, refresh, logout, me, verify.

**ПОЧЕМУ:** AuthService - это сердце auth-service. Он координирует работу KeycloakIntegrationService и TokenService для обеспечения аутентификации пользователей.

## Критерии выполнения

- [ ] Создан класс `AuthService` в пакете `com.autodev.auth.service`
- [ ] Реализован метод `login(LoginRequest request)` - аутентификация пользователя
  - [ ] Проверка учетных данных через KeycloakIntegrationService
  - [ ] Генерация токенов через TokenService
  - [ ] Возврат AuthResponse
- [ ] Реализован метод `refresh(String refreshToken)` - обновление токенов
  - [ ] Валидация refresh token через TokenService
  - [ ] Генерация новых токенов
  - [ ] Черный список старого refresh token
- [ ] Реализован метод `logout(String accessToken)` - выход пользователя
  - [ ] Добавление access token в черный список
  - [ ] 204 No Content ответ
- [ ] Реализован метод `getCurrentUser(String accessToken)` - получение текущего пользователя
  - [ ] Валидация access token
  - [ ] Получение keycloak_user_id из токена
  - [ ] Поиск пользователя по keycloak_user_id
  - [ ] Возврат UserDto
- [ ] Реализован метод `verifyToken(String token)` - валидация токена
  - [ ] Возврат true/false в зависимости от валидности
- [ ] Обработка исключений (AuthenticationException, InvalidTokenException)
- [ ] Кэширование данных в Redis (пользователь, роли)

## Обработка ошибок
```java
// Примеры ошибок
- 401 Unauthorized: неверные учетные данные
- 401 Unauthorized: истекший токен
- 401 Unauthorized: невалидный токен
- 429 Too Many Requests: слишком много попыток входа
```

## Ссылки
- [docs/architecture/api-specification/auth-service.yaml](../../docs/architecture/api-specification/auth-service.yaml) - endpoints
- [docs/architecture/security-strategy.md](../../docs/architecture/security-strategy.md) - стратегия безопасности

## Приоритет
Высокий

## Метки
backend, service, business-logic, auth-service
