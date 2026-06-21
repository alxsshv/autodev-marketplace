# Задача 004: Создать сущности и DTO для platform-service (UserProfileService)

**Статус:** Нужно создать

**GitLab задача:** #209 (https://alxsshv.com/Alxsshv/autodev-marketplace/-/work_items/209)

**Ветка:** `feature/004-platform-service-entities-dto`

---

## Описание

Текущее состояние: Проект platform-service - пустой проект с базовой структурой и зависимостями.

Требуется создать сущности JPA и DTO для управления профилями пользователей (UserProfileService для MVP), включая мапперы и миграции базы данных.

---

## Критерии выполнения

- [ ] Создан класс `UserEntity` в пакете `com.autodev.platform.model.entity`:
  - [ ] `@Entity` и `@Table(name = "users", schema = "platform")`
  - [ ] Поля: id (Long, @Id, @GeneratedValue), email (String, unique), username (String, unique), passwordHash (String), roles (Set<String>, @ElementCollection), createdAt (Instant)
  - [ ] Конструкторы, геттеры, сеттеры
- [ ] Создан класс `UserProfileEntity` в пакете `com.autodev.platform.model.entity`:
  - [ ] `@Entity` и `@Table(name = "user_profiles", schema = "platform")`
  - [ ] Поля: id (Long, @Id, @GeneratedValue), userId (Long, @OneToOne, @JoinColumn), firstName (String), lastName (String), phone (String), avatarUrl (String), createdAt (Instant), updatedAt (Instant)
  - [ ] Конструкторы, геттеры, сеттеры
- [ ] Создан класс `UserRegistrationDTO` для входящих данных:
  - [ ] Поля: email, username, password, firstName, lastName
  - [ ] Валидация (@NotBlank, @Email, @Size)
- [ ] Создан класс `UserProfileDTO` для исходящих данных:
  - [ ] Поля: userId, email, username, firstName, lastName, phone, avatarUrl, createdAt
- [ ] Создан класс `UserResponseDTO` для ответов:
  - [ ] Поля: userId, email, username, firstName, lastName, roles
- [ ] Создан интерфейс `UserMapper` с MapStruct
  - [ ] `@Mapper(componentModel = "spring")`
- [ ] Создана миграция Liquibase `v1.0.0/001-create-users-tables.yaml` в `services/platform-service/src/main/resources/db/migration/`:
  - [ ] Создание таблиц `platform.users` и `platform.user_profiles`
  - [ ] Создание индексов (email, username)
- [ ] Создана миграция Liquibase для начальных данных (роли BUYER, SELLER, MODERATOR)

---

## Архитектурные ссылки

- `docs/architecture/system-overview.md` - раздел 4 "Основные сущности" (User)
- `docs/architecture/business/services-for-mvp.md` - Platform Service (UserProfileService)
- `docs/coding-standards.md` - стандарты кодирования

---

## Приоритет

Высокий - фундамент для реализации UserProfileService.

---

**Создано:** 2026-06-21  
**Автор:** Системный аналитик  
**Предыдущая задача:** 003-4-api-gateway-service-token-filter  
**Следующая задача:** 005-platform-service-user-profile-service
