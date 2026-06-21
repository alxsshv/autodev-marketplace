# Задача 007: Реализовать сервисный слой ReviewService в platform-service

**Статус:** Нужно реализовать

**Ветка:** `feature/007-platform-service-review-service-impl`

---

## Описание

Текущее состояние: Проект platform-service содержит только UserProfileService.

Требуется реализовать `ReviewService` с методами создания, получения и обновления отзывов.

---

## Критерии выполнения

- [ ] Создан класс `ReviewService` с методами:
  - [ ] `createReview(CreateReviewDTO)` - создание отзыва
  - [ ] `getProductReviews(Long productId)` - получение отзывов для товара
  - [ ] `getUserReviews(Long userId)` - получение отзывов пользователя
  - [ ] `updateReview(Long reviewId, CreateReviewDTO)` - обновление отзыва
  - [ ] `deleteReview(Long reviewId)` - удаление отзыва
  - [ ] `calculateProductRating(Long productId)` - расчет среднего рейтинга товара
- [ ] Создан `ReviewController` с эндпоинтами:
  - [ ] `POST /api/v1/platform/reviews` - создание отзыва
  - [ ] `GET /api/v1/platform/reviews/products/{productId}` - получение отзывов для товара
  - [ ] `GET /api/v1/platform/reviews/users/{userId}` - получение отзывов пользователя
  - [ ] `PUT /api/v1/platform/reviews/{reviewId}` - обновление отзыва
  - [ ] `DELETE /api/v1/platform/reviews/{reviewId}` - удаление отзыва
- [ ] Модульные тесты для сервиса
- [ ] Интеграционные тесты для контроллера
- [ ] Обработка ошибок (ReviewNotFoundException, ReviewAccessDeniedException)

---

## Архитектурные ссылки

- `docs/architecture/system-overview.md` - Platform Service для MVP
- `docs/architecture/business/services-for-mvp.md` - ReviewService

---

## Приоритет

Средний - реализация функционала отзывов после профилей.

---

**Создано:** 2026-06-21  
**Автор:** Системный аналитик  
**Предыдущая задача:** 006-platform-service-review-service  
**Следующая задача:** 008-catalog-service-entities-dto
