# Задача 002: Исправить схемы базы данных PostgreSQL для сервисов MVP

**Статус:** Нужно исправить

**GitLab задача:** #204 (https://alxsshv.com/Alxsshv/autodev-marketplace/-/work_items/204)

**Ветка:** `feature/002-database-schemas-mvp`

---

## Описание

Текущее состояние: В файле `infrastructure/service-db/init/init-schemas.sql` созданы схемы для устаревшей архитектуры с отдельными сервисами (auth, user_service, catalog, pricing, inventory, search, order_service, payment, logistics, returns, notification, messaging, moderation, marketing, reporting, analytics, admin_service, seller_dashboard, seller_analytics, knowledgebase, parts_calculator, integration, videocall, media).

Требуется: Обновить схемы для MVP архитектуры (7 сервисов):
- platform-service → схема `platform`
- catalog-service → схема `catalog`
- order-service → схема `order`
- payment-service → схема `payment`
- communication-service → схема `communication`
- notification-service → схема `notification`
- api-gateway → не требует отдельной схемы (работает через Keycloak)

---

## Критерии выполнения

- [ ] Файл `infrastructure/service-db/init/init-schemas.sql` обновлен для MVP
- [ ] Созданы только необходимые схемы:
  - [ ] `platform` (для platform-service: UserProfileService, ReviewService)
  - [ ] `catalog` (для catalog-service: ProductCatalogService, CategoryService, VINLookupService)
  - [ ] `order` (для order-service: OrderService, CartService, DeliveryService, ReturnService)
  - [ ] `payment` (для payment-service: PaymentProcessingService, PaymentMethodService)
  - [ ] `communication` (для communication-service: MessagingService)
  - [ ] `notification` (для notification-service: NotificationService)
- [ ] Удалены избыточные схемы (auth, user_service, pricing, inventory, search, logistics, returns, messaging, moderation, marketing, reporting, analytics, admin_service, seller_dashboard, seller_analytics, knowledgebase, parts_calculator, integration, videocall, media)
- [ ] Предоставлены права пользователю `services_db_user` на все созданные схемы (активированы комментарии в SQL файле)

---

## Архитектурные ссылки

- `docs/architecture/system-overview.md` - раздел 1.3 "Контейнеры (C4 Level 2)" - 7 сервисов MVP
- `docs/architecture/business/services-for-mvp.md` - список сервисов для MVP

---

## Приоритет

Критический - без правильной схемы БД невозможно работать с сервисами.

---

## Решение

Обновить `infrastructure/service-db/init/init-schemas.sql` следующим образом:

```sql
-- Скрипт инициализации PostgreSQL для создания схем микросервисов MVP
-- Выполняется автоматически при первом запуске контейнера

-- Создание схем для MVP микросервисов
CREATE SCHEMA IF NOT EXISTS platform;
CREATE SCHEMA IF NOT EXISTS catalog;
CREATE SCHEMA IF NOT EXISTS order;
CREATE SCHEMA IF NOT EXISTS payment;
CREATE SCHEMA IF NOT EXISTS communication;
CREATE SCHEMA IF NOT EXISTS notification;

-- Предоставление прав пользователю services_db_user на все схемы
GRANT ALL PRIVILEGES ON SCHEMA platform TO ${SERVICES_DB_USER};
GRANT ALL PRIVILEGES ON SCHEMA catalog TO ${SERVICES_DB_USER};
GRANT ALL PRIVILEGES ON SCHEMA order TO ${SERVICES_DB_USER};
GRANT ALL PRIVILEGES ON SCHEMA payment TO ${SERVICES_DB_USER};
GRANT ALL PRIVILEGES ON SCHEMA communication TO ${SERVICES_DB_USER};
GRANT ALL PRIVILEGES ON SCHEMA notification TO ${SERVICES_DB_USER};

-- Предоставление прав на все таблицы в схемах (для новых и существующих)
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA platform TO ${SERVICES_DB_USER};
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA catalog TO ${SERVICES_DB_USER};
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA order TO ${SERVICES_DB_USER};
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA payment TO ${SERVICES_DB_USER};
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA communication TO ${SERVICES_DB_USER};
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA notification TO ${SERVICES_DB_USER};

-- Предоставление прав на все последовательности в схемах
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA platform TO ${SERVICES_DB_USER};
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA catalog TO ${SERVICES_DB_USER};
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA order TO ${SERVICES_DB_USER};
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA payment TO ${SERVICES_DB_USER};
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA communication TO ${SERVICES_DB_USER};
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA notification TO ${SERVICES_DB_USER};

-- Для новых таблиц, создаваемых в будущем
ALTER DEFAULT PRIVILEGES IN SCHEMA platform GRANT ALL ON TABLES TO ${SERVICES_DB_USER};
ALTER DEFAULT PRIVILEGES IN SCHEMA platform GRANT ALL ON SEQUENCES TO ${SERVICES_DB_USER};

ALTER DEFAULT PRIVILEGES IN SCHEMA catalog GRANT ALL ON TABLES TO ${SERVICES_DB_USER};
ALTER DEFAULT PRIVILEGES IN SCHEMA catalog GRANT ALL ON SEQUENCES TO ${SERVICES_DB_USER};

ALTER DEFAULT PRIVILEGES IN SCHEMA order GRANT ALL ON TABLES TO ${SERVICES_DB_USER};
ALTER DEFAULT PRIVILEGES IN SCHEMA order GRANT ALL ON SEQUENCES TO ${SERVICES_DB_USER};

ALTER DEFAULT PRIVILEGES IN SCHEMA payment GRANT ALL ON TABLES TO ${SERVICES_DB_USER};
ALTER DEFAULT PRIVILEGES IN SCHEMA payment GRANT ALL ON SEQUENCES TO ${SERVICES_DB_USER};

ALTER DEFAULT PRIVILEGES IN SCHEMA communication GRANT ALL ON TABLES TO ${SERVICES_DB_USER};
ALTER DEFAULT PRIVILEGES IN SCHEMA communication GRANT ALL ON SEQUENCES TO ${SERVICES_DB_USER};

ALTER DEFAULT PRIVILEGES IN SCHEMA notification GRANT ALL ON TABLES TO ${SERVICES_DB_USER};
ALTER DEFAULT PRIVILEGES IN SCHEMA notification GRANT ALL ON SEQUENCES TO ${SERVICES_DB_USER};
```

---

**Создано:** 2026-06-21  
**Автор:** Системный аналитик  
**Предыдущая задача:** 001-infrastructure-keycloak-service-accounts  
**Следующая задача:** 003-api-gateway-jwt-validation
