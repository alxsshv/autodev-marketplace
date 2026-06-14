# Техническое задание: Создание мапперов для DTO и Entity

## Название задачи
Создать мапперы для преобразования DTO и Entity (AUTH-004)

## Название ветки
feature/AUTH-004-create-mappers

## Описание
Для преобразования между DTO и Entity классами необходимо создать мапперы. Можно использовать MapStruct илиручную реализацию.

## Цель задачи
Создать мапперы для преобразования:
- `User` <-> `UserDto`
- `WebhookEvent` <-> `User` (для синхронизации)

## Критерии выполнения

- [ ] Создан `UserMapper` с методами:
  - `UserDto toDto(User user)`
  - `User toEntity(UserDto userDto)`

- [ ] Создан `WebhookEventMapper` с методами:
  - `User toEntity(WebhookEvent event)` (для синхронизации из Keycloak)

- [ ] Мапперы обрабатывают все поля с учетом различий в именовании (snake_case в DTO <-> camelCase в Entity)

- [ ] Все мапперы покрыты модульными тестами

- [ ] Добавлены Javadoc комментарии для всех мапперов

## Ссылки
- docs/architecture/api-specification/auth-service.yaml
- docs/coding-standards.md

## Приоритет
Средний

## Метки
backend, mapper, dto

## Сложность
Easy

## История изменений
| Версия | Дата | Автор | Описание |
|--------|------|-------|----------|
| 1.0 | 2026-06-14 | Системный аналитик | Создание задачи |
