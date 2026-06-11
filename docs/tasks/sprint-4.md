# Sprint 4: Основные API endpoints

**Эпик:** TS-003-SPRINT-4  
**Статус:** To Do  
**Приоритет:** Normal  
**Оценка времени:** 5 дней (40 часов)  
**Блокирует:** Sprint 5, Sprint 6, Sprint 7  
**Зависимости:** Sprint 1 (Base models), Sprint 2 (JWT Token Service), Sprint 3 (Security Configuration)

---

## Описание

Реализация основных API endpoints для аутентификации:
- POST /api/v1/auth/login — вход пользователя
- POST /api/v1/auth/refresh — обновление токенов
- POST /api/v1/auth/logout — выход пользователя
- GET /api/v1/auth/me — информация о текущем пользователе
- GET /api/v1/auth/validate-token — проверка токена (для API Gateway)

---

## GitLab Issues

| Issue | Название | Статус | Приоритет | Оценка | Блокирует |
|-------|----------|--------|-----------|--------|-----------|
| TS-003-4.1 | POST /api/v1/auth/login | To Do | Normal | 12 часов | Sprint 5 |
| TS-003-4.2 | POST /api/v1/auth/refresh | To Do | Normal | 8 часов | Sprint 5 |
| TS-003-4.3 | POST /api/v1/auth/logout | To Do | Normal | 6 часов | Sprint 5 |
| TS-003-4.4 | GET /api/v1/auth/me | To Do | Normal | 6 часов | Sprint 6 |
| TS-003-4.5 | GET /api/v1/auth/validate-token | To Do | Normal | 4 часа | Sprint 6 |
| TS-003-4.6 | Unit тесты API endpoints | To Do | Normal | 4 часа | Sprint 5 |

---

## Зависимости

**Этот спринт блокирует:**
- Sprint 5 (Keycloak Integration)
- Sprint 6 (Platform Service Integration)
- Sprint 7 (OAuth2 Provider Management)

**Зависит от:**
- Sprint 1 (Redis для rate limiting, revoked tokens)
- Sprint 2 (JWT Token Service — генерация и валидация токенов)
- Sprint 3 (Security Configuration — защита endpoints, JWT фильтр)

---

## Критерии готовности

- [ ] `POST /api/v1/auth/login` работает и возвращает пару токенов
- [ ] `POST /api/v1/auth/refresh` инвалидирует старый refresh token и генерирует новую пару
- [ ] `POST /api/v1/auth/logout` помечает токены как отозванные в Redis
- [ ] `GET /api/v1/auth/me` возвращает данные пользователя
- [ ] `GET /api/v1/auth/validate-token` проверяет токен и возвращает статус
- [ ] Покрытие unit тестов >80% для API endpoints
- [ ] Демонстрация: все endpoints работают через curl или Postman
