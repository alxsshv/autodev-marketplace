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

## GitLab Issues

| Issue | Название | Статус | Приоритет | Оценка | Блокирует |
|-------|----------|--------|-----------|--------|-----------|
| TS-003-9.1 | Admin endpoints | To Do | Normal | 16 часов | Sprint 9 |
| TS-003-9.2 | Unit тесты Admin endpoints | To Do | Normal | 8 часов | Sprint 9 |

---

## Зависимости

**Этот спринт не блокирует другие спринты** — добавляется к существующей функциональности.

**Зависит от:**
- Sprint 1 (Base models, Redis, PostgreSQL)
- Sprint 3 (Security Configuration — JWT фильтр, @PreAuthorize)
- Sprint 5 (Keycloak Integration — Admin API для создания/списка пользователей)

---

## Критерии готовности

- [ ] `GET /api/v1/admin/users` возвращает список пользователей (id, email, roles)
- [ ] `POST /api/v1/admin/users` создает пользователя в Keycloak и базе данных
- [ ] `PATCH /api/v1/admin/users/{id}/roles` обновляет роли пользователя
- [ ] `@PreAuthorize("hasRole('ADMIN')")` защищает все endpoints
- [ ] `403 Forbidden` возвращается для не-ADMIN пользователя
- [ ] Демонстрация: все endpoints работают через curl или Postman
