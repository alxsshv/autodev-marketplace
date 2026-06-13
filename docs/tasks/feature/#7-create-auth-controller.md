# Задача 7: Реализовать AuthController с endpoints

## Название задачи
Реализовать AuthController с endpoints для аутентификации

## Описание
Создать REST контроллер для обработки запросов к API аутентификации: login, refresh, logout, me, verify.

**ПОЧЕМУ:** Controller - это HTTP слой, который обрабатывает входящие запросы и вызывает бизнес-логику в AuthService.

## Критерии выполнения

- [ ] Создан класс `AuthController` в пакете `com.autodev.auth.controller`
- [ ] Контроллер аннотирован `@RestController` и `@RequestMapping("/api/v1/auth")`
- [ ] Реализован POST `/api/v1/auth/login` - вход пользователя
  - [ ] Валидация LoginRequest
  - [ ] Вызов AuthService.login()
  - [ ] Возврат AuthResponse (200) или Error (401, 429)
- [ ] Реализован POST `/api/v1/auth/refresh` - обновление токена
  - [ ] Валидация RefreshRequest
  - [ ] Вызов AuthService.refresh()
  - [ ] Возврат AuthResponse (200) или Error (401)
- [ ] Реализован POST `/api/v1/auth/logout` - выход пользователя
  - [ ] Проверка JWT токена (SecurityContext)
  - [ ] Вызов AuthService.logout()
  - [ ] Возврат 204 No Content
- [ ] Реализован GET `/api/v1/auth/me` - текущий пользователь
  - [ ] Проверка JWT токена
  - [ ] Вызов AuthService.getCurrentUser()
  - [ ] Возврат UserDto (200) или Error (401)
- [ ] Реализован GET `/api/v1/auth/verify` - валидация токена
  - [ ] Проверка JWT токена
  - [ ] Вызов AuthService.verifyToken()
  - [ ] Возврат 200 (валидно) или 401 (невалидно)

## Обработка исключений
```java
// Глобальный обработчик исключений
@ExceptionHandler(AuthenticationException.class)
@ResponseStatus(HttpStatus.UNAUTHORIZED)
public Error handleAuthenticationException(AuthenticationException ex)

@ExceptionHandler(InvalidTokenException.class)
@ResponseStatus(HttpStatus.UNAUTHORIZED)
public Error handleInvalidTokenException(InvalidTokenException ex)

@ExceptionHandler(RateLimitException.class)
@ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
public Error handleRateLimitException(RateLimitException ex)
```

## Ссылки
- [docs/architecture/api-specification/auth-service.yaml](../../docs/architecture/api-specification/auth-service.yaml) - OpenAPI спецификация
- [docs/architecture/glossary.md](../../docs/architecture/glossary.md) - HTTP коды ошибок

## Приоритет
Высокий

## Метки
backend, controller, rest-api, auth-service
