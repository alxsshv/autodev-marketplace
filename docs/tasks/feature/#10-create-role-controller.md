# Задача 10: Реализовать RoleController с endpoints

## Название задачи
Реализовать RoleController с endpoints для управления ролями

## Описание
Создать REST контроллер для обработки запросов к API управления ролями: список ролей, создание, обновление, удаление.

**ПОЧЕМУ:** Controller - это HTTP слой, который обрабатывает входящие запросы и вызывает бизнес-логику в RoleService.

## Критерии выполнения

- [ ] Создан класс `RoleController` в пакете `com.autodev.auth.controller`
- [ ] Контроллер аннотирован `@RestController` и `@RequestMapping("/api/v1/auth/roles")`
- [ ] Реализован GET `/api/v1/auth/roles` - список всех ролей
  - [ ] Вызов RoleService.getAllRoles()
  - [ ] Возврат List<RoleDto> (200)
- [ ] Реализован POST `/api/v1/auth/roles` - создание роли
  - [ ] Валидация CreateRoleRequest
  - [ ] Вызов RoleService.createRole()
  - [ ] Возврат RoleDto (201 Created) или Error (403, 409)
- [ ] Реализован GET `/api/v1/auth/roles/{roleId}` - получение роли
  - [ ] Вызов RoleService.getRoleById()
  - [ ] Возврат RoleDto (200) или Error (404)
- [ ] Реализован PUT `/api/v1/auth/roles/{roleId}` - обновление роли
  - [ ] Валидация UpdateRoleRequest
  - [ ] Вызов RoleService.updateRole()
  - [ ] Возврат RoleDto (200) или Error (404, 403)
- [ ] Реализован DELETE `/api/v1/auth/roles/{roleId}` - удаление роли
  - [ ] Вызов RoleService.deleteRole()
  - [ ] Возврат 204 No Content или Error (404, 403, 409)

## Проверка прав доступа
```java
// Только ADMIN может управлять ролями
@PreAuthorize("hasRole('ADMIN')")
@GetMapping
public List<RoleDto> getAllRoles()
```

## Ссылки
- [docs/architecture/api-specification/auth-service.yaml](../../docs/architecture/api-specification/auth-service.yaml) - OpenAPI спецификация
- [docs/architecture/security-strategy.md](../../docs/architecture/security-strategy.md) - RBAC права

## Приоритет
Средний

## Метки
backend, controller, rest-api, role-management
