# Sprint 6: Platform Service Integration

**Эпик:** TS-003-SPRINT-6  
**Статус:** To Do  
**Приоритет:** Normal  
**Оценка времени:** 4 дня (32 часа)  
**Блокирует:** Sprint 7  
**Зависимости:** Sprint 1 (Base models), Sprint 2 (JWT Token Service), Sprint 3 (Security Configuration), Sprint 4 (API endpoints), Sprint 5 (Keycloak Integration)

---

## Описание

Интеграция с Platform Service для синхронизации профилей пользователей:
- Синхронизация `auth.users` ↔ `platform_service.user_profiles`
- Kafka producer событий (auth.user_authenticated, auth.user_logout, platform.user_registered, platform.user_verified)
- Unit тесты интеграции с Platform Service API

---

## GitLab Issues

| Issue | Название | Статус | Приоритет | Оценка | Блокирует |
|-------|----------|--------|-----------|--------|-----------|
| TS-003-6.1 | PlatformServicePort (интерфейс) | To Do | Normal | 6 часов | Sprint 7 |
| TS-003-6.2 | Синхронизация профиля пользователя | To Do | Normal | 10 часов | Sprint 7 |
| TS-003-6.3 | GET /api/v1/auth/me с профилем | To Do | Normal | 6 часов | Sprint 7 |
| TS-003-6.4 | Kafka producer событий | To Do | Normal | 8 часов | Sprint 7 |

---

## Зависимости

**Этот спринт блокирует:**
- Sprint 7 (OAuth2 Provider Management)

**Зависит от:**
- Sprint 1 (Base models, PostgreSQL)
- Sprint 2 (JWT Token Service — проверка токена)
- Sprint 3 (Security Configuration — protected endpoints)
- Sprint 4 (API endpoints — login, refresh, logout, me)
- Sprint 5 (Keycloak Integration — вход пользователя)

---

## Критерии готовности

- [ ] Синхронизация `auth.users` ↔ `platform_service.user_profiles` работает
- [ ] `GET /api/v1/auth/me` возвращает данные из обоих сервисов
- [ ] Kafka producer публикует события (auth.*, platform.*)
- [ ] При ошибке интеграции — выполнение продолжается (не прерывается)
- [ ] Демонстрация: профиль синхронизирован, события публикуются
