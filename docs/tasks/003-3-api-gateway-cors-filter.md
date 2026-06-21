# Задача 003.3: Доработать API Gateway - реализовать CORSFilter для управления политиками CORS

**Статус:** Нужно реализовать

**GitLab задача:** #207 (https://alxsshv.com/Alxsshv/autodev-marketplace/-/work_items/207)

**Ветка:** `feature/003-3-api-gateway-cors-filter`

---

## Описание

Текущее состояние: В API Gateway отсутствует настройка CORS. Это приведет к блокировке запросов от фронтенд-приложений (React SPA) из-за политики Same-Origin Policy в браузерах.

Требуется реализовать `CORSFilter` для настройки политик CORS с разрешением запросов от доверенных источников.

---

## Критерии выполнения

- [ ] Создан класс `CORSFilter` в пакете `com.autodev.gateway.filter`
- [ ] Фильтр реализован как `GlobalFilter` в Spring Cloud Gateway
- [ ] Разрешенные origins настраиваются через application.yml
- [ ] Разрешенные методы: GET, POST, PUT, DELETE, OPTIONS, PATCH
- [ ] Разрешенные заголовки: Authorization, Content-Type, X-Request-ID, X-Language
- [ ] Поддержка preflight OPTIONS запросов (возврат 200 OK без передачи дальше)
- [ ] Добавление заголовка Access-Control-Allow-Credentials: true
- [ ] Тесты фильтра с проверкой заголовков для обычных и preflight запросов

---

## Архитектурные ссылки

- `docs/architecture/system-overview.md` - раздел 5 "Основные функциональные модули" (API Gateway)
- `docs/architecture/system-overview.md` - раздел 8 "Безопасность" (OWASP Top 10)

---

## Приоритет

Высокий - без CORS фильтра фронтенд-приложения не смогут взаимодействовать с API Gateway.

---

**Создано:** 2026-06-21  
**Автор:** Системный аналитик  
**Предыдущая задача:** 003-2-api-gateway-rate-limiting-filter  
**Следующая задача:** 003-4-api-gateway-service-token-filter
