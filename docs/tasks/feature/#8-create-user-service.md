# Задача 8: Реализовать UserService для управления пользователями

## Название задачи
Реализовать UserService для управления пользователями (Admin/Moderator)

## Описание
Создать сервис для управления пользователями с правами Admin/Moderator: список всех пользователей, получение по ID, обновление, удаление.

**ПОЧЕМУ:** Согласно OpenAPI, Admin и Moderator должны иметь возможность управлять пользователями. UserService координирует работу с KeycloakIntegrationService и UserRepository.

## Критерии выполнения

- [ ] Создан класс `UserService` в пакете `com.autodev.auth.service`
- [ ] Реализован метод `getAllUsers(Pageable pageable, String roleFilter)` - список пользователей
  - [ ] Пагинация (page, size)
  - [ ] Фильтрация по роли (опционально)
  - [ ] Возврат UserPage с content, total_elements, total_pages
- [ ] Реализован метод `getUserById(Long userId)` - получение пользователя
  - [ ] Поиск по ID в UserRepository
  - [ ] Возврат UserDto
  - [ ] 404 Not Found если пользователь не найден
- [ ] Реализован метод `updateUser(Long userId, UpdateUserRequest request)` - обновление пользователя
  - [ ] Валидация данных
  - [ ] Обновление в UserRepository
  - [ ] Обновление в Keycloak через KeycloakIntegrationService
  - [ ] Возврат обновленного UserDto
- [ ] Реализован метод `deleteUser(Long userId)` - удаление пользователя
  - [ ] Удаление из UserRepository
  - [ ] Удаление из Keycloak через KeycloakIntegrationService
  - [ ] 204 No Content ответ
- [ ] Проверка прав доступа (ADMIN или MODERATOR)
- [ ] Обработка исключений (UserNotFoundException, AccessDeniedException)

## Примеры ответов
```json
// GET /api/v1/auth/users?role=BUYER&page=0&size=20
{
  "content": [...],
  "total_elements": 100,
  "total_pages": 5,
  "number": 0,
  "size": 20
}
```

## Ссылки
- [docs/architecture/api-specification/auth-service.yaml](../../docs/architecture/api-specification/auth-service.yaml) - endpoints для управления пользователями
- [docs/architecture/security-strategy.md](../../docs/architecture/security-strategy.md) - RBAC права

## Приоритет
Средний

## Метки
backend, service, user-management, auth-service
