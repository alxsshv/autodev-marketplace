# ADR-0006: Keycloak как единственный источник правды для ролей

**Дата:** 2026-06-13  
**Статус:** Approved

---

## Контекст

Изначально в проекте планировалось хранить роли пользователей в PostgreSQL:
- Таблица `auth.roles` для хранения ролей (BUYER, SELLER, MODERATOR, ADMIN)
- Таблица `auth.permissions` для хранения прав
- Таблица `auth.role_permissions` для связи роль-права
- Поле `role` в таблице `platform_service.users`

Такой подход создавал дублирование данных: роли хранились и в Keycloak, и в PostgreSQL, что приводило к потенциальным рассинхронизациям и усложнял管理 ролей.

## Решение

Сделать Keycloak единственным источником правды (single source of truth) для ролей пользователей:

1. **Удалить таблицы из PostgreSQL:**
   - `auth.roles` - роли теперь хранятся только в Keycloak
   - `auth.permissions` - права теперь управляются через Keycloak
   - `auth.role_permissions` - связь роль-права через Keycloak

2. **Удалить поле `role` из `platform_service.users`:**
   - Роли пользователя определяются из JWT токена (выданного Keycloak)
   - Поле `role` больше не дублирует информацию из Keycloak

3. **Обновить процесс аутентификации:**
   - Keycloak выдаёт JWT токен с ролями пользователя
   - Все сервисы проверяют роли из JWT токена
   - Управление ролями осуществляется через Keycloak Admin Console или API

## Последствия

### Плюсы

- ✅ **Единый источник правды:** Роли хранятся только в Keycloak
- ✅ **Упрощение схемы БД:** Удалены 3 таблицы и 1 поле
- ✅ **Отсутствие рассинхронизации:** Нет риска, что роли в БД и Keycloak расходятся
- ✅ **Упрощённое управление:** Роли управляются через один интерфейс (Keycloak)
- ✅ **Совместимость:** JWT токен содержит все необходимые роли для авторизации

### Минусы

- ⚠️ Зависимость от Keycloak для проверки ролей
- ⚠️ Для аудита ролей нужно обращаться в Keycloak

### Компенсации

- Keycloak — устойчивый и проверенный продукт
- Кэширование ролей в Redis для снижения нагрузки на Keycloak
- JWT токен содержит роли, поэтому проверка работает даже при недоступности Keycloak (пока токен валиден)

## Альтернативы

1. **Оставить дублирование в PostgreSQL и Keycloak** — отклонено из-за риска рассинхронизации
2. **Хранить роли только в PostgreSQL** — отклонено как менее безопасный подход (требует дополнительной синхронизации с Keycloak)
3. **Хранить роли только в Redis кэше** — отклонено как менее надёжное решение (потеря кэша = потеря ролей)

---

## Зачем нужен Auth Service в архитектуре с Keycloak?

**Вопрос:** Если Keycloak уже управляет аутентификацией и ролями, зачем нужен auth-service?

**Ответ:** Auth Service служит **оберткой и адаптером** между Keycloak и микросервисной архитектурой:

1. **Синхронизация пользователей**
   - Keycloak хранит только аутентификационные данные (email, enabled)
   - PostgreSQL хранит связи с бизнес-данными (platform_service.users, loyalty_accounts)
   - Auth Service синхронизирует эти данные через Kafka события

2. **Кэширование для производительности**
   - Keycloak может быть узким местом при большом количестве запросов
   - JWT токены кэшируются в Redis (12 часов TTL)
   - Публичные ключи Keycloak кэшируются в Redis для быстрой валидации
   - Данные пользователей кэшируются в Redis (1 час TTL)

3. **Вспомогательные операции**
   - Logout (отзыв токена)
   - Get current user (из кэша Redis)
   - Get user by ID (из PostgreSQL для синхронизации)
   - Получение публичных ключей для валидации JWT

4. **Изоляция от Keycloak**
   - Микросервисы не зависят напрямую от Keycloak API
   - Изменения в Keycloak не требуют изменений в сервисах
   - Централизованная точка интеграции

5. **Роли пользователей**
   - Хранятся только в Keycloak
   - Выдаются в JWT токене при аутентификации
   - Проверяются каждым сервисом через валидацию JWT токена
   - Не синхронизируются в PostgreSQL (таблицы `auth.roles`, `auth.permissions`, `auth.role_permissions` удалены)
   - Не хранятся в поле `role` таблицы `platform_service.users` (удалено)

## Реализация

### Изменения в документации

- `docs/architecture/data-model.md` — удалены таблицы `auth.roles`, `auth.permissions`, `auth.role_permissions`
- `docs/architecture/data-model.md` — обновлена модель `platform_service.users` (удалено поле `role`)
- `docs/architecture/service-catalog/auth-service.md` — удалены endpoints управления ролями
- `docs/architecture/service-catalog/platform-service.md` — обновлена модель данных
- `docs/architecture/security-strategy.md` — обновлён раздел RBAC
- `docs/architecture/service-consolidation.md` — добавлен примечание о Keycloak
- `docs/architecture/roadmap.md` — добавлен примечание о ролях
- `docs/architecture/system-overview.md` — добавлен примечание о Keycloak
- `docs/architecture/glossary.md` — добавлено примечание об отсутствии таблиц ролей
- `docs/architecture/communication-policy.md` — обновлен пример JWT токена с ролями

### Изменения в инфраструктуре

- Удалить миграции для таблиц `auth.roles`, `auth.permissions`, `auth.role_permissions`
- Удалить миграцию для поля `role` в `platform_service.users`
- Обновить начальные данные (seed) для Keycloak (создать роли при установке)

### Изменения в коде сервисов

1. **Auth Service:**
   - Удалить endpoints управления ролями
   - Удалить сущности `Role`, `Permission`, `RolePermission`
   - Удалить репозитории для ролей

2. **Platform Service:**
   - Удалить поле `role` из сущности `User`
   - Удалить индекс по полю `role`
   - Обновить API endpoints, которые использовали поле `role`

3. **Другие сервисы:**
   - Обновить проверку ролей в аннотациях `@PreAuthorize`
   - Убедиться, что роли проверяются из JWT токена (через `hasRole()`)

## Ссылки

- [Keycloak Documentation - Roles](https://www.keycloak.org/documentation)
- [JWT Claims - roles/roles_claims](https://jwt.io/learn)
- [Spring Security - Role-Based Access Control](https://spring.io/guides/topicals/spring-security-architecture)

---

**Ответственные:** Архитектурная команда, Backend Team  
**Согласование:** Архитектор, Team Lead  
**Дата принятия:** 2026-06-13
