package com.autodev.platformservice.repository;

import com.autodev.platformservice.entity.ReviewEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * JPA-репозиторий для операций с таблицей отзывов (Reviews).
 * <p>
 * Предоставляет методы для сохранения, удаления и чтения отзывов
 * на товары/услуги платформы.
 * <p>
 * <b>Предопределённые методы запросов:</b>
 * <ul>
 *   <li>{@link #findByProductIdOrderByCreatedAtDesc(String)} — получение отзывов
 *       для конкретного товара, отсортированных по дате (новые первыми).</li>
 *   <li>{@link #findByUserId(String)} — получение всех отзывов, написанных конкретным пользователем.</li>
 * </ul>
 *
 * @see ReviewEntity
 * @see com.autodev.platformservice.controller.ReviewController
 */
@Repository
public interface ReviewRepository extends JpaRepository<ReviewEntity, UUID> {

    /**
     * Находит все отзывы для указанного товара, отсортированные по дате создания (новые первыми).
     * <p>
     * Используется для отображения списка отзывов на странице товара.
     * Сортировка по убыванию {@code createdAt} гарантирует, что свежие отзывы
     * отображаются вверху списка.
     *
     * @param productId идентификатор товара, для которого нужны отзывы
     * @return список отзывов для товара, отсортированный по убыванию даты создания
     */
    List<ReviewEntity> findByProductIdOrderByCreatedAtDesc(String productId);

    /**
     * Находит все отзывы, написанные указанным пользователем.
     * <p>
     * Используется для отображения истории отзывов пользователя
     * в личном кабинете или для модерации.
     *
     * @param userId идентификатор пользователя (ключевое поле Keycloak)
     * @return список отзывов пользователя
     */
    List<ReviewEntity> findByUserId(String userId);

}
