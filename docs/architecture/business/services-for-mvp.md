# AutoDev Marketplace — Сервисы для MVP и Post-MVP

**Версия документа:** 1.3  
**Дата создания:** 2026-06-21  
**Дата обновления:** 2026-06-21  
**Статус:** Документ архитектурного анализа

---

## Предисловие

Этот документ содержит список микросервисов, **необходимых для реализации MVP-функциональности** (32 функции), а также функционал для Post-MVP.

---

## MVP-функции и их реализация

### Категория 1: Поиск и каталог товаров (7 функций)

| Функция | Текущий сервис | Необходимость | Примечание |
|---------|---------------|--------------|------------|
| Поиск по VIN-коду | catalog-service | ✅ Да | VINLookupService |
| Поиск по артикулу | catalog-service | ✅ Да | ProductCatalogService |
| Поиск по названию | catalog-service | ✅ Да | PostgreSQL FTS (без Elasticsearch) |
| Поиск по марке/модели | catalog-service | ✅ Да | ProductCatalogService, CategoryService |
| Поиск по категории | catalog-service | ✅ Да | CategoryService |
| Фильтрация | catalog-service | ✅ Да | PostgreSQL FTS |
| Сортировка | catalog-service | ✅ Да | PostgreSQL |

**Вывод:** catalog-service — необходим для поиска и каталога (без search-service)

---

### Категория 2: Профили (3 функции)

| Функция | Текущий сервис | Необходимость | Примечание |
|---------|---------------|--------------|------------|
| Регистрация | platform-service | ✅ Да | Вызов Keycloak REST API напрямую |
| Аутентификация | api-gateway | ✅ Да | Keycloak JWT токен, валидация через Spring Security |
| Управление профилем | platform-service | ✅ Да | UserProfileService |



---

### Категория 3: Управление объявлениями (4 функции)

| Функция | Текущий сервис | Необходимость | Примечание |
|---------|---------------|--------------|------------|
| Создание объявления | catalog-service | ✅ Да | ProductCatalogService |
| Редактирование | catalog-service | ✅ Да | ProductCatalogService |
| Архивация | catalog-service | ✅ Да | ProductCatalogService |
| Продление | catalog-service | ✅ Да | ProductCatalogService |

**Вывод:** catalog-service — необходим для управления объявлениями

**Примечание:** Для MVP Catalog Service включает только базовый каталог с поиском (без аналогов, без TecDoc интеграции).

---

### Категория 4: Взаимодействие (1 функция)

| Функция | Текущий сервис | Необходимость | Примечание |
|---------|---------------|--------------|------------|
| Внутренний чат | communication-service | ✅ Да | MessagingService |

**Вывод:** communication-service — необходим для чата


---

### Категория 5: Оформление заказа (7 функций)

| Функция | Текущий сервис | Необходимость | Примечание |
|---------|---------------|---------------|------------|
| Добавление в корзину | order-service | ✅ Да          | CartService |
| Оформление заказа | order-service | ✅ Да          | OrderService |
| Оплата картой | payment-service | ✅ Да          |  базовая обработка без интеграций |
| Оплата при получении | order-service | ✅ Да          | OrderService (просто сохранение статуса) |
| Выбор доставки | order-service | ✅ Да          | DeliveryService (без расчета стоимости, базовый выбор ТК) |
| Идемпотентность | order-service | ✅ Да          | Паттерн на уровне сервиса |

**Вывод:** order-service + payment-service — необходимы для заказов


---

### Категория 6: Управление заказами (4 функции)

| Функция | Текущий сервис | Необходимость | Примечание |
|---------|---------------|--------------|------------|
| Просмотр заказов | order-service | ✅ Да | OrderService |
| Отслеживание статуса | order-service | ✅ Да | OrderService |
| Отмена заказа | order-service | ✅ Да | OrderService |
| Возврат товара | order-service | ✅ Да | ReturnService |

**Вывод:** order-service — необходим для управления заказами

---

### Категория 7: Рейтинги и отзывы (4 функции)

| Функция | Текущий сервис | Необходимость | Примечание |
|---------|---------------|--------------|------------|
| Оценка товара | platform-service | ✅ Да | ReviewService |
| Текстовый отзыв | platform-service | ✅ Да | ReviewService |
| Ответ продавца | platform-service | ✅ Да | ReviewService |
| Рейтинг продавца | platform-service | ✅ Да | ReviewService |

**Вывод:** platform-service — необходим для отзывов

---

### Категория 8: Уведомления (2 функции)

| Функция | Текущий сервис | Необходимость | Примечание |
|---------|---------------|--------------|------------|
| Email уведомления | notification-service | ✅ Да | NotificationService |
| SMS уведомления | notification-service | ✅ Да | NotificationService |

**Вывод:** notification-service — необходим для уведомлений

---

## Итоговый список сервисов для MVP

### Минимум микросервисов (7)

| Сервис | Обязательный | Описание |
|--------|-------------|----------|
| **api-gateway** | ✅ | Единая точка входа, маршрутизация, аутентификация |
| **catalog-service** | ✅ | Каталог товаров, поиск по VIN/артикулу, управление объявлениями |
| **order-service** | ✅ | Корзина, оформление заказов, управление заказами |
| **payment-service** | ✅ | Обработка платежей (для MVP: базовая, без эскроу и интеграций) |
| **communication-service** | ✅ | Чат (messaging) в реальном времени (для MVP: базовый функционал) |
| **notification-service** | ✅ | Email, SMS уведомления |
| **platform-service** | ✅ | Профили, отзывы, рейтинг продавцов (для MVP: только UserProfileService и ReviewService) |

**Убираем из MVP:**
- ❌ **auth-service** — использовать Keycloak напрямую через Spring Security OAuth2 Resource Server
- ❌ **search-service** — полнотекстовый поиск через PostgreSQL FTS
- ❌ **(нет)** — модерация (будет в admin-консоли для Post-MVP)

**Итого: 7 микросервисов для MVP**

---

## Platform Service: MVP vs Post-MVP

### MVP (минимум)
- `UserProfileService` - управление профилями пользователей (синхронизировано с Keycloak)
- `ReviewService` - управление отзывами и рейтингами


---

## Критерии MVP

1. **Минимальный функционал** — только то, без чего система не может работать
2. **Быстрый релиз** — MVP должен быть запущен в кратчайшие сроки
3. **Отсутствие излишеств** — всё дополнительное переносится на Post-MVP
4. **Ясные границы** — MVP и Post-MVP строго разделены

---

## План развития

### MVP (релиз 1.0)
- 7 микросервисов
- Базовая функциональность

---

### Аутентификация (Direct Keycloak Integration)

```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: ${KEYCLOAK_ISSUER_URI:http://localhost:8090/realms/autodev}
          jwk-set-uri: ${KEYCLOAK_JWK_SET_URI:http://localhost:8090/realms/autodev/protocol/openid-connect/certs}
```

**Цепочка аутентификации:**
1. User → api-gateway (POST /login)
2. api-gateway → Keycloak (POST /protocol/openid-connect/token)
3. Keycloak → JWT token → api-gateway → service
4. Service → Spring Security (валидация через Keycloak JWT Verifier)
5. Redis → кэш валидации токена

### Регистрация пользователя

**Цепочка регистрации:**
1. User → platform-service (POST /register)
2. platform-service → Keycloak REST API (create user)
3. platform-service → PostgreSQL (save user profile)
4. platform-service → Kafka (user_registered)
5. notification-service подписывается на user_registered (отправка welcome email)

---

## Рекомендация

### **Использовать 7 микросервисов с Direct Keycloak Integration (рекомендуется для MVP)**

1. **api-gateway** — маршрутизация, аутентификация, rate limiting
2. **catalog-service** — каталог товаров, поиск по VIN/артикулу, управление объявлениями (PostgreSQL + полнотекстовый поиск, без аналогов и TecDoc для MVP)
3. **order-service** — корзина, оформление, доставка, возвраты (без эскроу и расчёта стоимости доставки для MVP)
4. **payment-service** — базовая обработка платежей (без эскроу и интеграции с банками для MVP)
5. **communication-service** — чат в реальном времени (WebSocket, базовый функционал без шаблонов для MVP)
6. **notification-service** — email, SMS уведомления
7. **platform-service** — профили, отзывы, рейтинг (только UserProfileService и ReviewService для MVP)

**Ключевые изменения для MVP:**
- ✅ **Убираем auth-service** — использовать Keycloak напрямую через Spring Security OAuth2 Resource Server
- ✅ **Убираем search-service** — полнотекстовый поиск через PostgreSQL FTS
- ✅ **Разделяем уведомления и чат** — два отдельных сервиса
- ✅ **Управление пользователями в platform-service** — консолидировано из user-service
- ✅ **Упрощаем payment-service** — базовая обработка без эскроу и интеграций
- ✅ **Упрощаем catalog-service** — базовый каталог без аналогов и TecDoc интеграций
- ✅ **Упрощаем communication-service** — базовый чат без шаблонов и истории
- ✅ **Упрощаем order-service** — без эскроу и расчёта стоимости доставки
- ✅ **Упрощаем platform-service** — только UserProfileService и ReviewService (остальные функции — Post-MVP)

**Итого: 7 микросервисов для MVP (строгий минимум)**

---

## Platform Service: MVP 

### MVP (минимум)
- `UserProfileService` - управление профилями пользователей (синхронизировано с Keycloak)
- `ReviewService` - управление отзывами и рейтингами

---


