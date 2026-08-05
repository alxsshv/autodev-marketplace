package com.autodev.platformservice.repository;

import com.autodev.platformservice.entity.OutboxEntity;
import com.autodev.platformservice.entity.OutboxStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * JPA-репозиторий для операций с таблицей исходящих событий (Outbox).
 * <p>
 * Предоставляет методы для сохранения и чтения записей outbox,
 * используемых в паттерне SAGA Outbox для гарантии доставки событий.
 * <p>
 * <b>Основной сценарий использования:</b>
 * <ul>
 *   <li>{@link #findByStatusOrderByCreatedAtAsc(OutboxStatus, Pageable)} — чтение
 *       событий для последующей публикации в消息-брокер (Kafka, RabbitMQ).</li>
 *   <li>Наследуемый {@link JpaRepository#save(Object)} — запись новых событий.</li>
 * </ul>
 *
 * @see OutboxEntity
 * @see OutboxStatus
 * @see com.autodev.platformservice.service.OutboxService
 */
@Repository
public interface OutboxRepository extends JpaRepository<OutboxEntity, UUID> {

    /**
     * Находит события outbox с указанным статусом, отсортированные по дате создания (от старых к новым).
     * <p>
     * Используется компонентом-сапун (outbox poller) для периодического
     * считывания событий статуса {@code PENDING} и их публикации в消息-брокер.
     * <p>
     * <b>Параметры:</b>
     * <ul>
     *   <li>{@code status} — статус события для фильтрации (например, {@code PENDING}).</li>
     *   <li>{@code pageable} — параметры пагинации (например, {@code PageRequest.of(0, 50)}).</li>
     * </ul>
     * <p>
     * <b>Пример использования:</b>
     * <pre>{@code
     * List<OutboxEntity> pendingEvents = outboxRepository
     *     .findByStatusOrderByCreatedAtAsc(OutboxStatus.PENDING, PageRequest.of(0, 50));
     * }</pre>
     *
     * @param status статус событий для выборки
     * @param pageable параметры пагинации (страница, размер страницы, сортировка)
     * @return список событий с указанным статусом, отсортированный по возрастанию даты создания
     */
    List<OutboxEntity> findByStatusOrderByCreatedAtAsc(OutboxStatus status, Pageable pageable);

}
