# Sprint 3: Spring Security Configuration

**Эпик:** TS-003-SPRINT-3  
**Статус:** To Do  
**Приоритет:** Normal  
**Оценка времени:** 4 дня (32 часа)  
**Блокирует:** Sprint 4, Sprint 5, Sprint 6, Sprint 7, Sprint 8, Sprint 9  
**Зависимости:** Sprint 1 (Base models), Sprint 2 (JWT Token Service)

---

## Описание

Настройка Spring Security для Auth Service:
- JwtAuthenticationFilter для валидации JWT токенов
- SecurityConfig с @EnableMethodSecurity
- @PreAuthorize для защиты endpoints
- Unit тесты Security конфигурации

---

## GitLab Issues

| Issue | Название | Статус | Приоритет | Оценка | Блокирует |
|-------|----------|--------|-----------|--------|-----------|
| TS-003-3.1 | JwtAuthenticationFilter | To Do | Normal | 10 часов | Sprint 4 |
| TS-003-3.2 | SecurityConfig | To Do | Normal | 8 часов | Sprint 4 |
| TS-003-3.3 | Authentication объект | To Do | Normal | 4 часа | Sprint 4 |
| TS-003-3.4 | Security error responses | To Do | Normal | 4 часа | Sprint 4 |
| TS-003-3.5 | Unit тесты Security Configuration | To Do | Normal | 6 часов | Sprint 4 |

---

## Зависимости

**Этот спринт блокирует:**
- Sprint 4 (API endpoints)
- Sprint 5 (Keycloak Integration)
- Sprint 6 (Platform Service Integration)
- Sprint 7 (OAuth2 Provider Management)
- Sprint 8 (Rate Limiting)
- Sprint 9 (Admin Console)

**Зависит от:**
- Sprint 1 (Base models)
- Sprint 2 (JWT Token Service — JwtParser для валидации)

---

## Критерии готовности

- [ ] JwtAuthenticationFilter валидирует токены и создает Authentication объект
- [ ] SecurityConfig защищает endpoints через @PreAuthorize
- [ ] `401 Unauthorized` возвращается для невалидного токена
- [ ] `403 Forbidden` возвращается для недостатка прав
- [ ] Покрытие unit тестов >80% для Security конфигурации
- [ ] Демонстрация: filter проходит валидацию токена, защищенные endpoints возвращают 401/403
