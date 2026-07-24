package com.autodev.platformservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Сущность для реализации паттерна Transactional Outbox.
 * Используется для надежной доставки событий в Kafka через PostgreSQL.
 * 
 * <p>События сохраняются в отдельной таблице внутри транзакции с основной бизнес-логикой,
 * затем фоновый процесс отправляет их в Kafka и обновляет статус.</p>
 * 
 * @see OutboxStatus
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "outbox_events", schema = "platform")
public class OutboxEntity {

    /** Уникальный идентификатор события */
    @Id
    @UuidGenerator(style = UuidGenerator.Style.AUTO)
    @Column(name = "id",
            updatable = false, nullable = false, unique = true)
    private UUID id;

    /** Тип агрегата, к которому относится событие */
    @Column(name = "aggregate_type",
            nullable = false)
    private String aggregateType;

    /** Идентификатор агрегата */
    @Column(name = "aggregate_id",
            nullable = false)
    private UUID aggregateId;

    /** Тип события */
    @Column(name = "event_type",
            nullable = false)
    private String eventType;

    /** Название Kafka-топика, в который будет отправлено событие */
    @Column(name = "topic",
            nullable = false)
    private String topic;

    /** JSON-представление тела события */
    @Column(name = "payload",
            columnDefinition = "jsonb",
            nullable = false)
    private String payload;

    /** Временная метка создания события (автоматически устанавливается Hibernate) */
    @CreationTimestamp
    @Column(name = "created_at",
            updatable = false, nullable = false)
    private OffsetDateTime createdAt;

    /** Текущий статус отправки события */
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private OutboxStatus status;
}
