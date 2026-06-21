# Задача 003.2: Доработать API Gateway - реализовать RateLimitingFilter для ограничения запросов

**Статус:** Нужно реализовать

**GitLab задача:** #206 (https://alxsshv.com/Alxsshv/autodev-marketplace/-/work_items/206)

**Ветка:** `feature/003-2-api-gateway-rate-limiting-filter`

---

## Описание

Текущее состояние: В API Gateway отсутствует ограничение частоты запросов. Это делает систему уязвимой к DoS-атакам и злоупотреблениям.

Требуется реализовать `RateLimitingFilter` с использованием Resilience4j для ограничения количества запросов с одного IP-адреса.

---

## Критерии выполнения

- [ ] Создан класс `RateLimitingFilter` в пакете `com.autodev.gateway.filter`
- [ ] Фильтр реализован как `GlobalFilter` в Spring Cloud Gateway
- [ ] Используется Resilience4j RateLimiter
- [ ] Лимит запросов: 100 запросов в минуту на IP-адрес (настраивается через application.yml)
- [ ] При превышении лимита возвращается 429 Too Many Requests
- [ ] В ответе содержится заголовок Retry-After с временем ожидания
- [ ] IP-адрес извлекается из заголовка X-Forwarded-For или из RemoteAddress
- [ ] Тесты фильтра с проверкой лимита и 429 ответа

---

## Архитектурные ссылки

- `docs/architecture/system-overview.md` - раздел 5 "Основные функциональные модули" (API Gateway)
- `docs/architecture/system-overview.md` - раздел 8 "Безопасность" (OWASP Top 10 - A01:2021 Broken Access Control)

---

## Приоритет

Высокий - защита от DoS-атак и злоупотреблений.

---

**Создано:** 2026-06-21  
**Автор:** Системный аналитик  
**Предыдущая задача:** 003-1-api-gateway-authentication-filter  
**Следующая задача:** 003-3-api-gateway-cors-filter
