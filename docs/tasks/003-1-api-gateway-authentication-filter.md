# Задача 003.1: Доработать API Gateway - реализовать AuthenticationFilter для JWT валидации через Keycloak

**Статус:** Нужно реализовать

**GitLab задача:** #205 (https://alxsshv.com/Alxsshv/autodev-marketplace/-/work_items/205)

**Ветка:** `feature/003-1-api-gateway-authentication-filter`

---

## Описание

Текущее состояние: API Gateway имеет базовую конфигурацию с маршрутизацией, но отсутствует фильтр для валидации JWT токенов. Все клиентские запросы проходят без проверки аутентификации.

Требуется реализовать `AuthenticationFilter` для валидации JWT токенов через Keycloak (Direct Keycloak Integration без auth-service).

---

## Критерии выполнения

- [ ] Создан класс `AuthenticationFilter` в пакете `com.autodev.gateway.filter`
- [ ] Фильтр реализован как `GlobalFilter` в Spring Cloud Gateway
- [ ] Используется Spring Security OAuth2 Resource Server для валидации JWT
- [ ] Конфигурация issuer-uri и jwk-set-uri指向 Keycloak realm "autodev"
- [ ] При отсутствии токена возвращается 401 Unauthorized
- [ ] При невалидном токене (просрочен, подделка) возвращается 401 Unauthorized
- [ ] При валидном токене запрос передается дальше по цепочке
- [ ] Извлекаются и сохраняются claims из токена для использования в downstream сервисах
- [ ] Тесты фильтра с моками Keycloak (используется WireMock для эмуляции Keycloak JWK endpoint)

---

## Архитектурные ссылки

- `docs/architecture/system-overview.md` - раздел 5 "Основные функциональные модули" (API Gateway)
- `docs/architecture/system-overview.md` - раздел 8.2 "Межсервисная аутентификация" (Direct Keycloak Integration)
- `docs/architecture/api-specification/api-gateway.yaml` - security schemes `bearerAuth`

---

## Приоритет

Критический - без аутентификации невозможно обеспечить безопасность системы.

---

**Создано:** 2026-06-21  
**Автор:** Системный аналитик  
**Предыдущая задача:** -  
**Следующая задача:** 003-2-api-gateway-rate-limiting-filter
