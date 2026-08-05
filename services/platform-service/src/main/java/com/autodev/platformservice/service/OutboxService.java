package com.autodev.platformservice.service;

import com.autodev.platformservice.entity.DomainEvent;
import com.autodev.platformservice.entity.OutboxEntity;
import com.autodev.platformservice.entity.OutboxStatus;
import com.autodev.platformservice.repository.OutboxRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Сервис для реализации паттерна Outbox (SAGA Outbox).
 * <p>
 * Обеспечивает атомарное сохранение доменных событий вместе с транзакциями бизнес-логики.
 * События записываются в таблицу {@code outbox} в базе данных, а отдельный процесс-сапун
 * (outbox poller) периодически считывает события со статусом {@code PENDING} и публикует
 * их в消息-брокер (Kafka, RabbitMQ и т.д.).
 * <p>
 * <b>Преимущества паттерна Outbox:</b>
 * <ul>
 *   <li><b>Гарантия доставки:</b> событие сохраняется в базе данных той же транзакцией,
 *       которая изменяет состояние агрегата. Если транзакция откатывается, событие не сохраняется.</li>
 *   <li><b>Согласованность в итоге (eventual consistency):</b> сообщение будет опубликовано
 *       после успешного завершения бизнес-транзакции, даже если публикация происходит асинхронно.</li>
 *   <li><b>Идемпотентность:</b> статус {@code OutboxStatus.PENDING} позволяет безопасно
 *       перезапускать отложенную публикацию без дублирования событий.</li>
 * </ul>
 *
 * <h3>Рабочий процесс</h3>
 * <ol>
 *   <li>Бизнес-код вызывает {@link #publishEvent(DomainEvent, UUID, Object)} после изменения состояния агрегата.</li>
 *   <li>Событие сериализуется в JSON и сохраняется в {@code outbox} со статусом {@code PENDING}.</li>
 *   <li>Отдельный компонент (OutboxPoller) считывает события статусом {@code PENDING}
 *       и публикует их в消息-брокер.</li>
 *   <li>После успешной публикации статус изменяется на {@code SENT} или {@code FAILED}.</li>
 * </ol>
 *
 * <h3>Пример использования</h3>
 * <pre>{@code
 * // Внутри сервисного метода с @Transactional
 * userEventService.registerUser(dto);
 * outboxService.publishEvent(UserEvents.USER_REGISTERED, userId, dto);
 * }</pre>
 *
 * <h3>Потокобезопасность</h3>
 * <p>
 * Сервис является потокобезопасным, поскольку не хранит изменяемое состояние.
 * Все операции выполняются через инъектированные {@link OutboxRepository}
 * и {@link ObjectMapper}, которые также должны быть потокобезопасными.
 *
 * @see DomainEvent
 * @see com.autodev.platformservice.entity.UserEvents
 * @see OutboxEntity
 * @see OutboxStatus
 * @see OutboxRepository
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxService {

    /**
     * Репозиторий для сохранения и чтения событий из таблицы outbox.
     */
    private final OutboxRepository outboxRepository;

    /**
     * Объект для сериализации объектов полезной нагрузки в JSON-строки.
     * Инъектируется через Spring-конфигурацию.
     */
    private final ObjectMapper objectMapper;

    /**
     * Сохраняет доменное событие в outbox для последующей публикации.
     * <p>
     * Выполняется в рамках текущей транзакции (propagation = REQUIRED),
     * что гарантирует атомарность записи события вместе с другими
     * изменениями в базе данных.
     * <p>
     * <b>Алгоритм:</b>
     * <ol>
     *   <li>Сериализует объект {@code payload} в JSON-строку.</li>
     *   <li>Создаёт {@link OutboxEntity} с заполненными полями:
     *       {@code aggregateType}, {@code aggregateId}, {@code eventType},
     *       {@code topic}, {@code payload} и статусом {@code PENDING}.</li>
     *   <li>Сохраняет сущность через {@link OutboxRepository}.</li>
     * </ol>
     * <p>
     * <b>Обработка ошибок:</b>
     * <ul>
     *   <li>При ошибке сериализации ({@link JsonProcessingException}) логгируется ошибка
     *       с типом события и выбрасывается {@link RuntimeException} с сообщением
     *       {@code "Outbox serialization failed"}.</li>
     * </ul>
     *
     * @param event   тип доменного события (например, {@link com.autodev.platformservice.entity.UserEvents#USER_REGISTERED})
     * @param aggregateId идентификатор агрегата, к которому относится событие
     * @param payload объект полезной нагрузки (данные, связанные с событием)
     * @throws RuntimeException если не удалось сериализовать payload в JSON
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public void publishEvent(DomainEvent event, UUID aggregateId, Object payload) {
        try {
            String payloadJson = objectMapper.writeValueAsString(payload);

            OutboxEntity eventEntity = OutboxEntity.builder()
                    .aggregateType(event.getAggregateType())
                    .aggregateId(aggregateId)
                    .eventType(event.getEventType())
                    .topic(event.getTopic())
                    .payload(payloadJson)
                    .status(OutboxStatus.PENDING)
                    .build();

            outboxRepository.save(eventEntity);
        } catch (JsonProcessingException ex) {
            log.error("Failed to serialize outbox payload for event {}", event.getEventType(), ex);
            throw new RuntimeException("Outbox serialization failed", ex);

        }
    }

}
