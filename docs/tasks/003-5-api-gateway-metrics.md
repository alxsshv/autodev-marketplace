# Задача 003.5: Добавить метрики для API Gateway (счётчики ошибок аутентификации и других событий)

**Статус:** Нужно реализовать

**GitLab задача:** #216 (https://alxsshv.com/Alxsshv/autodev-marketplace/-/work_items/216)

**Ветка:** `feature/003-5-api-gateway-metrics`

---

## Описание

**Текущее состояние:** API Gateway использует Spring Boot Actuator и Micrometer для сбора метрик, но включены только базовые эндпоинты (`health`, `info`, `prometheus`). Нет специфических метрик для мониторинга работы фильтров безопасности (AuthenticationFilter, RateLimitingFilter и т.д.).

**Цель:** Добавить кастомные метрики для отслеживания:
- Ошибок аутентификации (по типам: отсутствие токена, невалидный формат, проверка через Keycloak)
- Срабатываний Rate Limiting (превышение лимита)
- CORS preflight запросов
- Межсервисных вызовов с невалидными service tokens
- Общего количества запросов по маршрутам

Это позволит в будущем создавать алерты в Grafana/Prometheus на основе этих метрик и проводить аудит безопасности.

---

## Критерии выполнения

- [ ] Создан класс `GatewayMetrics` в пакете `com.autodev.gateway.metrics` с инъекцией `MeterRegistry`
- [ ] Добавлены метрики для AuthenticationFilter:
  - `gateway.auth.failed` - счетчик ошибок аутентификации с тегами: `reason` (missing_header, invalid_format, validation_failed), `path`, `ip`
- [ ] Добавлены метрики для RateLimitingFilter:
  - `gateway.rate_limited` - счетчик срабатываний rate limiting с тегами: `ip`, `path`
- [ ] Добавлены метрики для CORSFilter:
  - `gateway.cors.preflight` - счетчик preflight запросов с тегами: `origin`, `method`
- [ ] Добавлены метрики для ServiceTokenFilter:
  - `gateway.service_token.invalid` - счетчик невалидных service tokens с тегами: `client_id`, `reason`
  - `gateway.service_token.forbidden` - счетчик запрещенных client_id с тегами: `client_id`
- [ ] Все метрики добавляются через `counter()` метод MeterRegistry
- [ ] Метрики интегрированы в существующие фильтры (AuthenticationFilter и т.д.)
- [ ] Метрики доступны через `/actuator/prometheus` endpoint
- [ ] Написаны юнит-тесты для проверки добавления метрик (используя `MockMeterRegistry` или WireMock)

---

## Архитектурные ссылки

- `docs/architecture/system-overview.md` - раздел 5 "Основные функциональные модули" (API Gateway)
- `docs/architecture/system-overview.md` - раздел 8 "Безопасность" - аудит безопасности
- `docs/architecture/system-overview.md` - раздел 8.5 "План аудита безопасности" - еженедельные проверки логов аутентификации
- `docs/tasks/003-1-api-gateway-authentication-filter.md` - задача по AuthenticationFilter
- `docs/tasks/003-2-api-gateway-rate-limiting-filter.md` - задача по RateLimitingFilter

---

## Приоритет

Средний - метрики не критичны для функциональности, но необходимы для мониторинга и аудита. Без них невозможно настроить алерты на ошибки аутентификации и other security events.

---

## Пояснение

Согласно архитектуре проекта, для мониторинга используются Prometheus и Grafana. В текущей конфигурации Actuator включает только базовые метрики. Кастомные метрики необходимы для:

1. **Еженедельного аудита безопасности** - отслеживание неудачных попыток входа, rate limiting events
2. **Настройки алертов** - предупреждения о высоком количестве ошибок аутентификации (возможная атака)
3. **Диагностики проблем** - понимание, какие фильтры срабатывают чаще всего
4. **Соблюдения OWASP Top 10** - требования к logging и monitoring (A09:2021)

Метрики будут доступны через `/actuator/prometheus`, который уже включён в конфигурации, и интегрируются в существующую инфраструктуру мониторинга.

---

**Создано:** 2026-06-24  
**Автор:** Системный аналитик  
**Предыдущая задача:** 003-4-api-gateway-service-token-filter  
**Следующая задача:** 006-api-gateway-routes-mvp
