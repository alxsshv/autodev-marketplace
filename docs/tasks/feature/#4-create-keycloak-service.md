# Задача 4: Реализовать KeycloakIntegrationService

## Название задачи
Реализовать KeycloakIntegrationService для интеграции с Keycloak

## Описание
Создать сервис для взаимодействия с Keycloak Admin REST API. Этот сервис будет создавать, обновлять и удалять пользователей в Keycloak.

**ПОЧЕМУ:** Согласно архитектуре, auth-service использует Keycloak как источник идентификации. Все операции с пользователями проходят через Keycloak API. Auth service хранит только аутентификационные данные (id, keycloak_user_id, email, enabled).

## Критерии выполнения

- [ ] Создан пакет `com.autodev.auth.integration.keycloak`
- [ ] Создан класс `KeycloakIntegrationService` с `@Service` аннотацией
- [ ] Реализован метод `createUser(String email, String password)` - создание пользователя в Keycloak
- [ ] Реализован метод `getUserById(String keycloakUserId)` - получение пользователя из Keycloak
- [ ] Реализован метод `updateUser(String keycloakUserId, UserUpdateRequest request)` - обновление пользователя
- [ ] Реализован метод `deleteUser(String keycloakUserId)` - удаление пользователя
- [ ] Реализован метод `updatePassword(String keycloakUserId, String newPassword)` - обновление пароля
- [ ] Настроены конфигурации Keycloak (URL, realm, client credentials)
- [ ] Используется Spring WebClient или RestTemplate для вызова Keycloak API
- [ ] Обработка ошибок и retry логика

## Конфигурация Keycloak
```yaml
keycloak:
  server-url: ${KEYCLOAK_URL:http://localhost:8090}
  realm: master
  client-id: admin-cli
  client-secret: ${KEYCLOAK_CLIENT_SECRET}
  user: ${KEYCLOAK_ADMIN_USER}
  password: ${KEYCLOAK_ADMIN_PASSWORD}
```

## Ссылки
- [docs/architecture/security-strategy.md](../../docs/architecture/security-strategy.md) - стратегия аутентификации
- [docs/architecture/data-model.md](../../docs/architecture/data-model.md) - модель данных auth-service
- [docker-compose.yaml](../../docker-compose.yaml) - конфигурация Keycloak

## Приоритет
Высокий

## Метки
backend, integration, keycloak, auth-service
