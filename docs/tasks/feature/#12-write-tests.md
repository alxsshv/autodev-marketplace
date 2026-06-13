# Задача 12: Написать модульные и интеграционные тесты

## Название задачи
Написать модульные и интеграционные тесты для auth-service

## Описание
Создать комплекс тестов для проверки работы всех компонентов auth-service: сервисы, контроллеры, репозитории.

**ПОЧЕМУ:** Тесты обеспечивают качество кода, предотвращают регрессии и позволяют безопасно рефакторить код.

## Критерии выполнения

### Модульные тесты (Unit Tests)
- [ ] Тесты для `TokenService` (с моками)
  - [ ] generateTokens() - генерация токенов
  - [ ] validateAccessToken() - валидация
  - [ ] isTokenBlacklisted() - проверка черного списка
- [ ] Тесты для `AuthService` (с моками Keycloak, TokenService)
  - [ ] login() - позитивный и негативный сценарии
  - [ ] refresh() - обновление токенов
  - [ ] logout() - выход пользователя
  - [ ] me() - текущий пользователь
  - [ ] verifyToken() - валидация токена
- [ ] Тесты для `UserService` (с моками UserRepository, KeycloakIntegrationService)
  - [ ] getAllUsers() - пагинация
  - [ ] getUserById() - получение пользователя
  - [ ] updateUser() - обновление
  - [ ] deleteUser() - удаление
- [ ] Тесты для `RoleService` (с моками RoleRepository)
  - [ ] getAllRoles() - список ролей
  - [ ] createRole() - создание роли
  - [ ] updateRole() - обновление
  - [ ] deleteRole() - удаление

### Интеграционные тесты (Integration Tests)
- [ ] Тесты для `AuthController` (MockMvc)
  - [ ] POST /api/v1/auth/login - позитивный и негативный сценарии
  - [ ] POST /api/v1/auth/refresh - обновление токенов
  - [ ] POST /api/v1/auth/logout - выход
  - [ ] GET /api/v1/auth/me - текущий пользователь
  - [ ] GET /api/v1/auth/verify - валидация токена
- [ ] Тесты для `User controller` (MockMvc, ADMIN/MODERATOR права)
  - [ ] GET /api/v1/auth/users - список пользователей
  - [ ] GET /api/v1/auth/users/{userId} - получение
  - [ ] PUT /api/v1/auth/users/{userId} - обновление
  - [ ] DELETE /api/v1/auth/users/{userId} - удаление
- [ ] Тесты для `Role controller` (MockMvc, ADMIN права)
  - [ ] GET /api/v1/auth/roles - список ролей
  - [ ] POST /api/v1/auth/roles - создание
  - [ ] PUT /api/v1/auth/roles/{roleId} - обновление
  - [ ] DELETE /api/v1/auth/roles/{roleId} - удаление

### Интеграция с реальными компонентами
- [ ] Тесты с Testcontainers для реального Keycloak
- [ ] Тесты с реальной PostgreSQL базой
- [ ] Тесты с реальным Redis (если используется кэширование)

## Покрытие кода
- Unit тесты: минимум 80% покрытие
- Integration тесты: все основные сценарии

## Пример теста
```java
@Test
void shouldLoginSuccessfully() {
    LoginRequest request = new LoginRequest("user@example.com", "Password123!");
    
    when(keycloakIntegrationService.authenticate(any(), any())).thenReturn(true);
    when(tokenService.generateTokens(any(), any())).thenReturn(authResponse);
    
    AuthResponse response = authService.login(request);
    
    assertNotNull(response);
    assertNotNull(response.getAccessToken());
    assertNotNull(response.getRefreshToken());
}
```

## Ссылки
- [docs/architecture/testing-strategy.md](../../docs/architecture/testing-strategy.md) - стратегия тестирования
- [docs/coding-standards.md](../../docs/coding-standards.md) - стандарты кодирования

## Приоритет
Средний

## Метки
testing, unit-tests, integration-tests, auth-service
