# Задача 9: Реализовать RoleService для управления ролями

## Название задачи
Реализовать RoleService для управления ролями (Admin only)

## Описание
Создать сервис для управления ролями с правами Admin: список всех ролей, создание, обновление, удаление.

**ПОЧЕМУ:** Согласно OpenAPI, только Admin должен управлять ролями. RoleService координирует работу с RoleRepository.

## Критерии выполнения

- [ ] Создан класс `RoleService` в пакете `com.autodev.auth.service`
- [ ] Реализован метод `getAllRoles()` - список всех ролей
  - [ ] Возврат List<RoleDto>
- [ ] Реализован метод `createRole(CreateRoleRequest request)` - создание роли
  - [ ] Валидация: name уникален, не null
  - [ ] Проверка: роль не существует
  - [ ] Сохранение в RoleRepository
  - [ ] Возврат RoleDto (201 Created)
- [ ] Реализован метод `getRoleById(Long roleId)` - получение роли
  - [ ] Поиск по ID в RoleRepository
  - [ ] Возврат RoleDto
  - [ ] 404 Not Found если роль не найдена
- [ ] Реализован метод `updateRole(Long roleId, UpdateRoleRequest request)` - обновление роли
  - [ ] Валидация: роль существует
  - [ ] Обновление в RoleRepository
  - [ ] Возврат обновленного RoleDto
- [ ] Реализован метод `deleteRole(Long roleId)` - удаление роли
  - [ ] Проверка: роль не используется пользователями
  - [ ] Удаление из RoleRepository
  - [ ] 204 No Content ответ
- [ ] Проверка прав доступа (ADMIN only)
- [ ] Обработка исключений (RoleNotFoundException, AccessDeniedException, RoleInUseException)

## Обработка ошибок
```java
// Примеры ошибок
- 403 Forbidden: недостаточно прав (не ADMIN)
- 404 Not Found: роль не найдена
- 409 Conflict: роль уже существует
- 409 Conflict: роль используется пользователями
```

## Ссылки
- [docs/architecture/api-specification/auth-service.yaml](../../docs/architecture/api-specification/auth-service.yaml) - endpoints для управления ролями
- [docs/architecture/security-strategy.md](../../docs/architecture/security-strategy.md) - RBAC права

## Приоритет
Средний

## Метки
backend, service, role-management, auth-service
