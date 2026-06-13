# Задачи для Auth Service

В этом каталоге находятся задачи для реализации сервиса аутентификации (auth-service) в проекте AutoDev Marketplace.

## Обзор

**Цель:** Реализовать сервис аутентификации с интеграцией Keycloak

**Стек:** Java 17, Spring Boot 3.4.5, Spring Security, JWT, Keycloak, PostgreSQL, Redis

**Ключевые требования:**
- Интеграция с Keycloak (обязательно)
- Auth-service управляет ТОЛЬКО аутентификационными данными (id, keycloak_user_id, email, enabled)
- Все бизнес-данные пользователя хранятся в `platform_service.users`
- OpenAPI спецификация: `docs/architecture/api-specification/auth-service.yaml`

## Задачи

### Приоритет 1 (Критичные для MVP)
1. **#1-fix-user-entity** - Исправить сущность UserEntity для соответствия модели данных
2. **#2-create-role-repository** - Создать RoleRepository интерфейс
3. **#3-create-auth-dto** - Создать DTO классы для аутентификации
4. **#4-create-keycloak-service** - Реализовать KeycloakIntegrationService
5. **#5-create-token-service** - Реализовать TokenService для JWT
6. **#6-create-auth-service** - Реализовать AuthService с бизнес-логикой
7. **#7-create-auth-controller** - Реализовать AuthController с endpoints
8. **#11-setup-security** - Настроить Spring Security с JWT

### Приоритет 2 (Дополнительные функции)
9. **#8-create-user-service** - Реализовать UserService для управления пользователями (Admin/Moderator)
10. **#9-create-role-service** - Реализовать RoleService для управления ролями (Admin only)
11. **#10-create-role-controller** - Реализовать RoleController с endpoints
12. **#12-write-tests** - Написать модульные и интеграционные тесты

## Порядок выполнения

Задачи должны выполняться в порядке возрастания номера (от #1 к #12), так как каждая следующая задача зависит от результатов предыдущей.

### Шаги выполнения:
1. Создать ветку из `develop` с именем `feature/#<номер>-<название-задачи>`
2. Выполнить задачу согласно описанию в соответствующем файле
3. Написать тесты (если не описано отдельно)
4. Создать Pull Request
5. После ревью иmerge в `develop`

## Ссылки на документацию

- [Архитектура](../../docs/architecture/)
- [OpenAPI спецификация](../../docs/architecture/api-specification/auth-service.yaml)
- [Данные и модели](../../docs/architecture/data-model.md)
- [Безопасность](../../docs/architecture/security-strategy.md)
- [Глоссарий](../../docs/architecture/glossary.md)
- [Роадмап](../../docs/architecture/roadmap.md)

## Важные замечания

1. Все коды должны соответствовать соглашениям из `docs/architecture/glossary.md`
2. Миграции базы данных через Liquibase
3. Код должен соответствовать `docs/coding-standards.md`
4. Все endpoints должны соответствовать OpenAPI спецификации
5. В случае сомнений - обращаться к архитектору

## Успех выполнения

После выполнения всех 12 задач:
- ✅ Auth-service запускается и регистрируется в Consul
- ✅ Все endpoints работают согласно OpenAPI спецификации
- ✅ Интеграция с Keycloak работает корректно
- ✅ JWT токены генерируются и валидируются
- ✅ RBAC работает (BUYER, SELLER, MODERATOR, ADMIN)
- ✅ Все модульные и интеграционные тесты проходят
