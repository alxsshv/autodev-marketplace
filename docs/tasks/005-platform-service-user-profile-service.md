# Задача 005: Реализовать базовый сервисный слой platform-service (UserProfileService)

**Статус:** Нужно реализовать

**GitLab задача:** #210 (https://alxsshv.com/Alxsshv/autodev-marketplace/-/work_items/210)

**Ветка:** `feature/005-platform-service-user-profile-service`

---

## Описание

Текущее состояние: Проект platform-service не содержит бизнес-логики.

Требуется реализовать `UserProfileService` с методами регистрации пользователя, синхронизации с Keycloak и управления профилем.

---

## Критерии выполнения

- [ ] Создан класс `UserProfileService` с методами:
  - [ ] `registerUser(UserRegistrationDTO)` - регистрация пользователя с синхронизацией в Keycloak
  - [ ] `getUserProfile(Long userId)` - получение профиля пользователя
  - [ ] `updateUserProfile(Long userId, UserProfileDTO)` - обновление профиля
- [ ] Создан `UserProfileController` с эндпоинтами:
  - [ ] `POST /api/v1/platform/users/register` - регистрация
  - [ ] `GET /api/v1/platform/users/profile` - получение профиля
  - [ ] `PUT /api/v1/platform/users/profile` - обновление профиля
- [ ] Интеграция с Keycloak через REST API (создание пользователя в Keycloak)
- [ ] Обработка ошибок (UserAlreadyExistsException, UserNotFoundException и т.д.)
- [ ] Модульные тесты для сервиса с моками Keycloak
- [ ] Интеграционные тесты для контроллера

---

## Архитектурные ссылки

- `docs/architecture/system-overview.md` - Platform Service для MVP
- `docs/architecture/business/services-for-mvp.md` - UserProfileService

---

## Приоритет

Высокий - реализация основного функционала профилей пользователей.

---

**Создано:** 2026-06-21  
**Автор:** Системный аналитик  
**Предыдущая задача:** 004-platform-service-entities-dto  
**Следующая задача:** 006-platform-service-review-service
