# Sprint 9: Admin Console

**Эпик:** TS-003-SPRINT-9  
**Статус:** To Do  
**Приоритет:** Normal  
**Оценка времени:** 3 дня (24 часа)  
**Блокирует:** Нет (дополнительная функциональность для MVP)  
**Зависимости:** Sprint 1 (Base models), Sprint 3 (Security Configuration), Sprint 5 (Keycloak Integration)

---

## Описание

Функционал Admin Console для управления пользователями:
- Создание пользователя через Admin API
- Список всех пользователей
- Обновление ролей пользователя

---

## Задачи

### 9.1. Admin endpoints (для MVP)

**Оценка:** 16 часов  
**Критерии приёмки:**
- AC-9.1: `AdminController.getUsers()` — `GET /api/v1/admin/users` список всех пользователей (поля: id, email, roles)
- AC-9.2: `AdminController.createUser()` — `POST /api/v1/admin/users` создание пользователя (email + password, создание в Keycloak через Admin API)
- AC-9.3: `AdminController.updateUserRoles()` — `PATCH /api/v1/admin/users/{id}/roles` обновление ролей (`{ "roles": ["BUYER"] }`)
- AC-9.4: Все endpoints защищены `@PreAuthorize("hasRole('ADMIN')")`
- AC-9.5: `403 Forbidden` для обычного пользователя (не ADMIN)
- AC-9.6: Unit тест `AdminControllerTest.getUsers()` проверяет список пользователей

**Зависимости:** Sprint 1 (Base models), Sprint 3 (Security Configuration), Sprint 5 (Keycloak Integration — Admin API)

---

### 9.2. Unit тесты Admin endpoints

**Оценка:** 8 часов  
**Критерии приёмки:**
- AC-9.7: `AdminControllerTest.getUsers()` — проверка списка пользователей
- AC-9.8: `AdminControllerTest.createUser()` — проверка создания пользователя
- AC-9.9: `AdminControllerTest.updateUserRoles()` — проверка обновления ролей
- AC-9.10: `AdminControllerTest.adminProtected()` — проверка 403 для не-ADMIN пользователя

**Зависимости:** 9.1 (Admin endpoints)

---

## Критерии готовности спринта

- [ ] `GET /api/v1/admin/users` возвращает список пользователей (id, email, roles)
- [ ] `POST /api/v1/admin/users` создает пользователя в Keycloak и базе данных
- [ ] `PATCH /api/v1/admin/users/{id}/roles` обновляет роли пользователя
- [ ] `@PreAuthorize("hasRole('ADMIN')")` защищает все endpoints
- [ ] `403 Forbidden` возвращается для не-ADMIN пользователя
- [ ] Демонстрация: все endpoints работают через curl или Postman

---

## Зависимости от других спринтов

**Этот спринт не блокирует другие спринты** — добавляется к существующей функциональности.

**Зависит от:**
- Sprint 1 (Base models, Redis, PostgreSQL)
- Sprint 3 (Security Configuration — JWT фильтр, @PreAuthorize)
- Sprint 5 (Keycloak Integration — Admin API для создания/списка пользователей)

---

## GitLab issue structure

```
TS-003-SPRINT-5: Sprint 5: Keycloak Integration (Epic)
├── TS-003-SPRINT-9: Sprint 9: Admin Console (Epic)
│   ├── TS-003-9.1: Admin endpoints (Issue)
│   └── TS-003-9.2: Unit тесты Admin endpoints (Issue)
```

---

## Примечания

- Admin Console реализован через existing endpoints из ТЗ TS-003
- Все операции требуют роль ADMIN (проверка через @PreAuthorize)
- Создание пользователя создает запись и в Keycloak, и в PostgreSQL
- Обновление ролей обновляет только роли (не email, не password)
