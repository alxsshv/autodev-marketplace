# Задача 001: Настроить Realm "autodev" и Service Accounts в Keycloak

**Статус:** Нужно исправить

**GitLab задача:** #203 (https://alxsshv.com/Alxsshv/autodev-marketplace/-/work_items/203)

**Ветка:** `feature/001-keycloak-service-accounts`

---

## Описание

Текущее состояние: Keycloak запущен, realm называется "autoparts" (а не "autodev" как требуется в документации), service accounts для микросервисов не настроены.

Требуется создать realm "autodev" с правильной конфигурацией и service accounts для каждого микросервиса.

---

## Критерии выполнения

- [ ] Realm называется "autodev" (а не "autoparts")
- [ ] В realm "autodev" созданы service accounts для всех 7 микросервисов MVP:
  - [ ] `api-gateway-service` (client type: service, client id: api-gateway)
  - [ ] `platform-service` (client type: service, client id: platform-service)
  - [ ] `catalog-service` (client type: service, client id: catalog-service)
  - [ ] `order-service` (client type: service, client id: order-service)
  - [ ] `payment-service` (client type: service, client id: payment-service)
  - [ ] `communication-service` (client type: service, client id: communication-service)
  - [ ] `notification-service` (client type: service, client id: notification-service)
- [ ] Все service accounts имеют включенную опцию "Service Accounts Enabled"
- [ ] Для каждого service account сгенерирован пароль (сохранен в `.env` файл)
- [ ] Realm конфигурация соответствует `docs/architecture/system-overview.md` раздел 8.2 (Direct Keycloak Integration)

---

## Архитектурные ссылки

- `docs/architecture/system-overview.md` - раздел 8.2 "Межсервисная аутентификация"
- `docs/architecture/api-specification/api-gateway.yaml` - security schemes `bearerAuth` и `serviceBearerAuth`

---

## Приоритет

Критический - без настроенного Keycloak невозможно работать с аутентификацией.

---

## Примечания

Текущий realm "autoparts" должен быть либо переименован в "autodev", либо удален и создан новый realm "autodev" с нуля.

Service account пароли должны быть сохранены в `.env` файле проекта в формате:
```
API_GATEWAY_SERVICE_CLIENT_SECRET=...
PLATFORM_SERVICE_CLIENT_SECRET=...
CATALOG_SERVICE_CLIENT_SECRET=...
ORDER_SERVICE_CLIENT_SECRET=...
PAYMENT_SERVICE_CLIENT_SECRET=...
COMMUNICATION_SERVICE_CLIENT_SECRET=...
NOTIFICATION_SERVICE_CLIENT_SECRET=...
```

---

**Создано:** 2026-06-21  
**Автор:** Системный аналитик  
**Предыдущая задача:** -  
**Следующая задача:** 002-infrastructure-database-services
