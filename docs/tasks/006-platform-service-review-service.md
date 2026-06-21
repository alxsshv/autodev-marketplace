# Задача 006: Создать сущности и DTO для ReviewService в platform-service

**Статус:** Нужно создать

**Ветка:** `feature/006-platform-service-review-service`

---

## Описание

Текущее состояние: Проект platform-service содержит только UserProfileService.

Требуется создать сущности JPA и DTO для управления отзывами и рейтингами (ReviewService для MVP).

---

## Критерии выполнения

- [ ] Создан класс `ReviewEntity` в пакете `com.autodev.platform.model.entity`:
  - [ ] `@Entity` и `@Table(name = "reviews", schema = "platform")`
  - [ ] Поля: id (Long, @Id, @GeneratedValue), productId (Long), userId (Long), rating (Integer), text (String), createdAt (Instant), updatedAt (Instant), isVerified (Boolean)
  - [ ] Конструкторы, геттеры, сеттеры
- [ ] Создан класс `ReviewDTO` для исходящих данных:
  - [ ] Поля: id, productId, userId, username, rating, text, createdAt, isVerified
- [ ] Создан класс `CreateReviewDTO` для входящих данных:
  - [ ] Поля: productId, rating, text
  - [ ] Валидация (@Min(1), @Max(5), @NotBlank)
- [ ] Создан класс `ReviewResponseDTO` для ответов:
  - [ ] Поля: id, productId, rating, text, createdAt, isVerified
- [ ] Создан интерфейс `ReviewMapper` с MapStruct
- [ ] Создана миграция Liquibase `v1.0.0/003-create-reviews-table.yaml`:
  - [ ] Создание таблицы `platform.reviews`
  - [ ] Создание индексов (productId, userId)
- [ ] Создана миграция Liquibase `v1.0.0/004-create-ratings-table.yaml`:
  - [ ] Создание таблицы `platform.ratings` для агрегации рейтингов товаров

---

## Архитектурные ссылки

- `docs/architecture/system-overview.md` - раздел 4 "Основные сущности" (Review)
- `docs/architecture/business/services-for-mvp.md` - Platform Service (ReviewService)

---

## Приоритет

Высокий - фундамент для реализации ReviewService.

---

**Создано:** 2026-06-21  
**Автор:** Системный аналитик  
**Предыдущая задача:** 005-platform-service-user-profile-service  
**Следующая задача:** 007-platform-service-review-service-impl
