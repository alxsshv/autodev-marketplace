# Задача 2: Создать RoleRepository интерфейс

## Название задачи
Создать RoleRepository интерфейс для управления ролями

## Описание
Создать репозиторий для доступа к сущности Role. Репозиторий должен предоставлять CRUD-операции и специфичные методы для работы с ролями.

**ПОЧЕМУ:** Репозиторий - это абстракция над доступом к данным. Без него не будет возможности управлять ролями в базе данных.

## Критерии выполнения

- [ ] Создан интерфейс `RoleRepository` в пакете `com.autodev.auth.repository`
- [ ] Интерфейс расширяет `JpaRepository<Role, Long>`
- [ ] Реализован метод `findByName(String name)` для поиска роли по имени
- [ ] Реализован метод `findAll()` для получения всех ролей (унаследованный от JpaRepository)
- [ ] Репозиторий аннотирован `@Repository`
- [ ] Миграции для таблицы roles уже созданы (`07-06-2026-create-table-roles.sql`)

## Ссылки
- [docs/architecture/data-model.md](../../docs/architecture/data-model.md) - раздел "Схема: auth" (таблица roles)
- [services/auth-service/src/main/resources/db/changelog/v1.0.0/07-06-2026-create-table-roles.sql](../../services/auth-service/src/main/resources/db/changelog/v1.0.0/07-06-2026-create-table-roles.sql)
- [services/auth-service/src/main/java/com/autodev/auth/entity/Role.java](../../services/auth-service/src/main/java/com/autodev/auth/entity/Role.java)

## Приоритет
Высокий

## Метки
backend, database, repository, auth-service
