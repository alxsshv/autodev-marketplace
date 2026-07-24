package com.autodev.platformservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "outbox_events", schema = "platform")
public class OutboxEntity {

    @Id
    @UuidGenerator(style = UuidGenerator.Style.AUTO)
    @Column(name = "id",
            updatable = false, nullable = false, unique = true)
    private UUID id;

    @Column(name = "aggregate_type",
            nullable = false)
    private String aggregateType;

    @Column(name = "aggregate_id",
            nullable = false)
    private UUID aggregateId;

    @Column(name = "event_type",
            nullable = false)
    private String eventType;

    @Column(name = "topic",
            nullable = false)
    private String topic;

    @Column(name = "payload",
            columnDefinition = "jsonb",
            nullable = false)
    private String payload;

    @CreationTimestamp
    @Column(name = "created_at",
            updatable = false, nullable = false)
    private OffsetDateTime createdAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private OutboxStatus status;
}
