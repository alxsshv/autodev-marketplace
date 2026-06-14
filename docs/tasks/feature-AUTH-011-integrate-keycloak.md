# Техническое задание: Интеграция с Keycloak для получения публичных ключей

## Название задачи
Интегрировать Keycloak для получения публичных ключей (AUTH-011)

## Название ветки
feature/AUTH-011-integrate-keycloak

## Описание
Необходимо настроить HTTP client для получения публичных ключей Keycloak для валидации JWT токенов.

## Цель задачи
Создать HTTP client для получения публичных ключей Keycloak и кэширования их.

## Критерии выполнения

- [ ] Создан `KeycloakClient` с методом `getPublicKeys()`:
  - Выполняет HTTP GET запрос к `/realms/{realm}/protocol/openid-connect/certs`
  - Возвращает `KeycloakPublicKeyResponse`

- [ ] Создан `KeycloakPublicKeyCacheService`:
  - Метод `getPublicKeys()` - получает ключи из кэша или Keycloak
  - Кэширование ключей с TTL (например, 1 hour)
  - Обновление кэша при истечении TTL

- [ ] Настроена обработка ошибок:
  - Retry механизм (3 попытки)
  - Fallback на кэш при падении Keycloak
  - Логирование ошибок

- [ ] Создан `JwtDecoderProvider`:
  - Использует кэшированные ключи для создания `JwtDecoder`
  - Проверяет валидность ключей

- [ ] Все сервисы покрыты модульными тестами с моками HTTP client

- [ ] Добавлены Javadoc комментарии для всех методов

## Ссылки
- docs/architecture/api-specification/auth-service.yaml - endpoint GET /api/v1/auth/keys
- docs/architecture/security/jwt-structure.md

## Приоритет
Средний

## Метки
backend, keycloak, http-client

## Сложность
Medium

## История изменений
| Версия | Дата | Автор | Описание |
|--------|------|-------|----------|
| 1.0 | 2026-06-14 | Системный аналитик | Создание задачи |
