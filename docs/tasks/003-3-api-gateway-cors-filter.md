# Задача 003.3: Настроить CORS в API Gateway для поддержки фронтенд‑приложений

**Статус:** Нужно реализовать  
**GitLab задача:** #207 (https://alxsshv.com/Alxsshv/autodev-marketplace/-/work_items/207)  
**Ветка:** `feature/003-3-api-gateway-cors-filter`

---

## Описание

API Gateway должен корректно обрабатывать CORS‑запросы от фронтенд‑приложения (React SPA), включая preflight‑запросы (OPTIONS).

С учётом текущего стека (Spring Cloud Gateway, Spring WebFlux, Resilience4j, Testcontainers, WireMock) необходимо использовать **встроенную поддержку CORS из Spring WebFlux**, а не реализовывать кастомный фильтр. Это обеспечит корректную обработку preflight‑запросов на уровне фреймворка, избежит конфликтов с роутингом и упростит поддержку.

---

## Критерии выполнения

- [ ] CORS настроен через секцию `spring.webflux.cors` в `application.yml`.
- [ ] Список разрешённых origins вынесен в отдельное свойство `cors.allowed-origins` (тип — список строк), без использования wildcard `*` при включённом `allow-credentials`.
- [ ] Разрешены методы: `GET, POST, PUT, DELETE, OPTIONS, PATCH`.
- [ ] Разрешены заголовки: `Authorization, Content-Type, X-Request-ID, X-Language`.
- [ ] Включён параметр `allow-credentials: true` для поддержки передачи куки и авторизационных заголовков.
- [ ] В `application.yml` отсутствует кастомная реализация CORS через `GlobalFilter` или иные фильтры.
- [ ] Реализованы интеграционные тесты с использованием `WebTestClient`, покрывающие следующие сценарии:
    - [ ] Preflight‑запрос (OPTIONS) на произвольный маршрут возвращает статус `200 OK` и содержит корректные заголовки `Access-Control-Allow-Methods`, `Access-Control-Allow-Headers`, `Access-Control-Allow-Origin`.
    - [ ] Обычный запрос (например, GET) с заголовком `Origin` возвращает ответ с корректным `Access-Control-Allow-Origin` (конкретный origin, а не `*`).
    - [ ] Проверка, что при запросе с недоверенным `Origin` CORS‑заголовки либо не добавляются, либо поведение соответствует политике безопасности (на усмотрение реализации Spring WebFlux).
- [ ] Тесты размещены в пакете `com.autodev.gateway` и наследуются от `AbstractIntegrationTest` с использованием `Testcontainers` и `AutoConfigureWebTestClient`.
- [ ] В тестах не поднимается реальный фронтенд — проверка осуществляется исключительно через HTTP‑запросы с эмуляцией CORS‑поведения.

---

## Требования к конфигурации (пример)

```yaml
spring:
  webflux:
    cors:
      mappings:
        '/**':
          allowed-origins: "${cors.allowed-origins:#{T(java.util.Arrays).asList('http://localhost:3000','http://app.example.com')}}"
          allowed-methods: GET, POST, PUT, DELETE, OPTIONS, PATCH
          allowed-headers: Authorization, Content-Type, X-Request-ID, X-Language
          allow-credentials: true
```



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
