# Задача 003-1: Настроить Spring Security OAuth2 Resource Server для JWT валидации в API Gateway

**Статус:** Нужно реализовать

**GitLab задача:** #205 (https://alxsshv.com/Alxsshv/autodev-marketplace/-/work_items/205)

**Ветка:** `feature/003-1-api-gateway-security-config`

---

## Описание

**Контекст:** 
API Gateway должен выступать в роли OAuth2 Resource Server и выполнять аутентификацию входящих запросов через валидацию JWT токенов от Keycloak (Direct Integration).

Ключевое архитектурное правило: Gateway отвечает за проверку подлинности (кто ты), а downstream‑сервисы отвечают за проверку прав (что тебе можно). Gateway не должен быть единственным источником истины для ролей пользователя.

**Текущее состояние:** 

- В application.yml настроен issuer-uri для Keycloak realm autodev.
- Spring Security OAuth2 Resource Server подключён, но security filter chain не настроен.
- Сейчас все маршруты либо открыты, либо не защищены должным образом.

**Требуется:**

Создать SecurityConfig класс в пакете com.autodev.gateway.config с bean‑ом SecurityWebFilterChain, который:

- Отключает CSRF (API stateless).
- Настраивает oauth2ResourceServer().jwt() для валидации JWT.
- Определяет правила доступа к маршрутам.

Настроить правила доступа:

- Открытые маршруты (без аутентификации):

/actuator/** — Actuator.

/swagger-ui/**, /v3/api-docs/**, /swagger-resources/** — Swagger/OpenAPI.

- Защищённые маршруты: все остальные (например, /api/v1/** и т. д.) требуют валидного JWT.

- Поведение при отсутствии/невалидном токене: возвращать 401 Unauthorized с заголовком WWW-Authenticate: Bearer.

***Обработка JWT и пробрасывание контекста:***

Использовать JwtAuthenticationConverter для извлечения claims и создания Authentication с Principal, содержащим:

- userId (sub)
- email (email)
- name (preferred_username или name)
- (опционально) roles — только для удобства логирования в Gateway, не как источник прав.

Пробрасывать в downstream‑сервисы:

Обязательно: оригинальный заголовок Authorization: Bearer <JWT> (чтобы downstream мог сам валидировать токен и проверять роли).

Рекомендуется: X-User-Id, X-User-Email, X-User-Name — для логирования, аудита, персонализации.

Не использовать как источник прав: X-User-Roles. Downstream‑сервисы не должны принимать решения о доступе только на основе этого заголовка.

OpenTelemetry Trace Context: передавать заголовки traceparent и tracestate во все downstream‑запросы (стандартное поведение Gateway, убедиться, что фильтр не удаляет их).

**Тесты:**

***Модульные тесты:*** использовать SecurityMockMvcRequestPostProcessors.jwt() для эмуляции токена и проверки правил authorizeExchange (открытые vs защищённые пути).

***Интеграционные тесты:***

Использовать WireMock для эмуляции Keycloak (.well-known/openid-configuration и JWKS endpoint).

Проверять, что:

- Запрос без токена → 401.
- Запрос с невалидным токеном → 401.
- Запрос с валидным токеном проходит через Gateway.

В downstream‑запросе присутствует заголовок Authorization и безопасные атрибуты (X-User-Id и т.п.).
---

## Критерии выполнения

- [ ] Создан SecurityConfig в com.autodev.gateway.config.

- [ ] Настроен SecurityWebFilterChain: CSRF отключён, oauth2ResourceServer().jwt(), правила authorizeExchange для открытых и защищённых маршрутов.

- [ ] Реализован JwtAuthenticationConverter, который извлекает sub, email, name, и опционально roles.

- [ ] В Gateway реализован фильтр (или логика), который:

Не удаляет заголовок Authorization.

Добавляет X-User-Id, X-User-Email, X-User-Name.

Не полагается на X-User-Roles для принятия решений о доступе.

- [ ] OpenTelemetry заголовки (traceparent, tracestate) корректно пробрасываются.

- [ ] Модульные тесты проверяют правила доступа и поведение на отсутствие токена.

- [ ] Интеграционные тесты (WireMock) подтверждают, что Gateway валидирует JWT и корректно маршрутизирует запросы.

- [ ] В документации/комментариях явно указано: downstream‑сервисы должны самостоятельно валидировать JWT и проверять роли, а не доверять заголовкам.

---

## Архитектурные ссылки

- `docs/architecture/system-overview.md` - раздел 5 "Основные функциональные модули" (API Gateway)
- `docs/architecture/system-overview.md` - раздел 8.2 "Межсервисная аутентификация" (Direct Keycloak Integration)
- `docs/architecture/api-specification/api-gateway.yaml` - security schemes `bearerAuth`


**Создано:** 2026-07-01  
**Автор:** Системный аналитик  
**Предыдущая задача:** -  
**Следующая задача:** 003-2-api-gateway-rate-limiting-filter
