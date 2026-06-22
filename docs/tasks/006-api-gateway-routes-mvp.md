# Задача 006: Исправить маршруты API Gateway в соответствии с MVP архитектурой

**Статус:** Нужно исправить

**GitLab задача:** #211 (https://alxsshv.com/Alxsshv/autodev-marketplace/-/work_items/211)

**Ветка:** `feature/006-api-gateway-routes-mvp`

---

## Описание

В файле `services/api-gateway/src/main/resources/application.yml` необходимо обновить маршруты в соответствии с MVP архитектурой.

## Маршруты для MVP архитектуры:

| ID маршрута | Сервис | Паттерн | RewritePath |
|-------------|--------|---------|-------------|
| platform-service | platform-service | /api/v1/platform/** | /$\${path} |
| catalog-service | catalog-service | /api/v1/catalog/** | /$\${path} |
| order-service | order-service | /api/v1/orders/** | /$\${path} |
| payment-service | payment-service | /api/v1/payments/** | /$\${path} |
| communication-service | communication-service | /api/v1/communication/** | /$\${path} |
| notification-service | notification-service | /api/v1/notifications/** | /$\${path} |

---

## Критерии выполнения

- [ ] Обновлены маршруты для platform-service (подмена user-service)
- [ ] Обновлены маршруты для catalog-service (объединение catalog-service, pricing-inventory-service, search-service)
- [ ] Обновлены маршруты для order-service (оставить без изменений)
- [ ] Обновлены маршруты для payment-service (оставить без изменений)
- [ ] Обновлены маршруты для communication-service (новый сервис)
- [ ] Обновлены маршруты для notification-service (оставить без изменений)
- [ ] Удалены избыточные маршруты (auth-service, pricing-inventory-service, search-service, review-service, recommendation-service, admin-service, analytics-service)
- [ ] Обновлены фильтры RewritePath для новых сервисов

---

## Архитектурные ссылки

- `docs/architecture/system-overview.md` - раздел 3.0 (Названия сервисов и директорий)
- `docs/architecture/business/services-for-mvp.md` - 7 микросервисов для MVP

---

## Приоритет

Средний - маршруты должны соответствовать реальной архитектуре.

---

**Создано:** 2026-06-21  
**Автор:** Системный аналитик  
**Предыдущая задача:** 005-platform-service-user-profile-service  
**Следующая задача:** -
