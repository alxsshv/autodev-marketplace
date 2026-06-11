# Sprint 7: OAuth2 Provider Management

**Эпик:** TS-003-SPRINT-7  
**Статус:** To Do  
**Приоритет:** Normal  
**Оценка времени:** 4 дня (32 часа)  
**Блокирует:** Нет (финальный спринт для MVP)  
**Зависимости:** Sprint 1 (Base models), Sprint 2 (JWT Token Service), Sprint 3 (Security Configuration), Sprint 4 (API endpoints), Sprint 5 (Keycloak Integration), Sprint 6 (Platform Service Integration)

---

## Описание

Управление OAuth2 провайдерами:
- Таблица `oauth_providers` в PostgreSQL
- Привязка/отвязка OAuth2 аккаунтов
- Синхронизация профиля из OAuth2 провайдера
- Unit тесты для всех операций

---

## GitLab Issues

| Issue | Название | Статус | Приоритет | Оценка | Блокирует |
|-------|----------|--------|-----------|--------|-----------|
| TS-003-7.1 | oauth_providers таблица | To Do | Normal | 6 часов | Sprint 7 |
| TS-003-7.2 | OAuthProviderPort (интерфейс) | To Do | Normal | 4 часа | Sprint 7 |
| TS-003-7.3 | Основные endpoints | To Do | Normal | 12 часов | Sprint 7 |
| TS-003-7.4 | Синхронизация профиля | To Do | Normal | 8 часов | Sprint 7 |

---

## Зависимости

**Этот спринт не блокирует другие спринты** — финальный спринт для MVP.

**Зависит от:**
- Sprint 1 (Base models, PostgreSQL, Redis)
- Sprint 2 (JWT Token Service — проверка токена)
- Sprint 3 (Security Configuration — protected endpoints)
- Sprint 4 (API endpoints — base endpoints)
- Sprint 5 (Keycloak Integration — для базовой аутентификации)
- Sprint 6 (Platform Service Integration — синхронизация профиля)

---

## Критерии готовности

- [ ] `POST /api/v1/auth/external/link` привязывает OAuth2 аккаунт
- [ ] `POST /api/v1/auth/external/unlink` отвязывает OAuth2 аккаунт (но не последний)
- [ ] `GET /api/v1/auth/external/providers` возвращает список привязанных провайдеров
- [ ] Синхронизация профиля из OAuth2 провайдера работает
- [ ] Демонстрация: привязка, отвязка, синхронизация профиля
