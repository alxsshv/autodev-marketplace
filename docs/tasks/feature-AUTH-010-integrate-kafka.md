# Техническое задание: Интеграция с Kafka для синхронизации событий

## Название задачи
Интегрировать Kafka (AUTH-010)

## Название ветки
feature/AUTH-010-integrate-kafka

## Описание
Необходимо настроить Kafka для отправки событий при создании/обновлении пользователей, чтобы другие сервисы (platform-service) могли синхронизировать данные.

## Цель задачи
Настроить Kafka producers для отправки событий:
- `auth.user_registered` - при создании нового пользователя
- `auth.user_updated` - при обновлении данных пользователя
- `auth.user_enabled` - при активации пользователя
- `auth.user_disabled` - при деактивации пользователя

## Критерии выполнения

- [ ] Создан `KafkaProducerConfig` с настройками подключения к Kafka

- [ ] Создан `UserEventProducer`:
  - Метод `sendUserRegistered(String keycloakUserId, String email)` - отправляет событие `auth.user_registered`
  - Метод `sendUserUpdated(String keycloakUserId, String email)` - отправляет событие `auth.user_updated`
  - Метод `sendUserEnabled(String keycloakUserId)` - отправляет событие `auth.user_enabled`
  - Метод `sendUserDisabled(String keycloakUserId)` - отправляет событие `auth.user_disabled`

- [ ] Структура событий соответствует спецификации:
  ```json
  {
    "event": "auth.user_registered",
    "timestamp": "2026-06-14T10:00:00Z",
    "payload": {
      "keycloak_user_id": "123e4567-e89b-12d3-a456-426614174000",
      "email": "user@example.com",
      "created_at": "2026-06-14T10:00:00Z"
    }
  }
  ```

- [ ] Обработка ошибок (retry механизм)

- [ ] Все producers покрыты модульными тестами

- [ ] Добавлены Javadoc комментарии для всех методов

## Ссылки
- docs/architecture/eventual-consistency.md
- docs/architecture/security/user-registration-architecture.md

## Приоритет
Средний

## Метки
backend, kafka, messaging

## Сложность
Medium

## История изменений
| Версия | Дата | Автор | Описание |
|--------|------|-------|----------|
| 1.0 | 2026-06-14 | Системный аналитик | Создание задачи |
