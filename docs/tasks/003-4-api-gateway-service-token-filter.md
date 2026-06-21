# Задача 003.4: Доработать API Gateway - реализовать ServiceTokenFilter для межсервисной аутентификации

**Статус:** Нужно реализовать

**GitLab задача:** #208 (https://alxsshv.com/Alxsshv/autodev-marketplace/-/work_items/208)

**Ветка:** `feature/003-4-api-gateway-service-token-filter`

---

## Описание

Текущее состояние: В API Gateway отсутствует фильтр для проверки service tokens при межсервисных вызовах. Каждый сервис должен иметь service account в Keycloak и передавать JWT токен при вызовах других сервисов.

Требуется реализовать `ServiceTokenFilter` для валидации service tokens и проверки client_id в токене.

---

## Критерии выполнения

- [ ] Создан класс `ServiceTokenFilter` в пакете `com.autodev.gateway.filter`
- [ ] Фильтр реализован как `GlobalFilter` в Spring Cloud Gateway
- [ ] Извлекает service token из заголовка Authorization: Bearer {token}
- [ ] Валидирует token через Keycloak (issuer-uri + jwk-set-uri)
- [ ] Проверяет client_id в token (должен соответствовать service account)
- [ ] Допускает service accounts только для разрешенных сервисов (из конфигурации)
- [ ] Возвращает 401 Unauthorized для невалидных токенов
- [ ] Возвращает 403 Forbidden для токенов с неразрешенными client_id
- [ ] Тесты фильтра с моками Keycloak (WireMock)

---

## Архитектурные ссылки

- `docs/architecture/system-overview.md` - раздел 8.2 "Межсервисная аутентификация"
- `docs/architecture/api-specification/api-gateway.yaml` - security schemes `serviceBearerAuth`

---

## Приоритет

Высокий - без проверки service tokens невозможно обеспечить безопасность межсервисных вызовов.

---

**Создано:** 2026-06-21  
**Автор:** Системный аналитик  
**Предыдущая задача:** 003-3-api-gateway-cors-filter  
**Следующая задача:** 004-platform-service-entities-dto
