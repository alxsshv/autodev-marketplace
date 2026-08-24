package com.autodev.catalogservice.entity;


import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Абстрактный базовый класс для всех сущностей.
 *
 * <p>Предоставляет общую функциональность для всех сущностей приложения:
 * <ul>
 *     <li>Уникальный идентификатор (UUID)</li>
 *     <li>Время создания записи</li>
 *     <li>Время последнего обновления</li>
 * </ul></p>
 */
@Getter
@Setter
@MappedSuperclass
public class AbstractBaseEntity {

    /** Уникальный идентификатор сущности */
    @Id
    @UuidGenerator(style = UuidGenerator.Style.RANDOM)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    /** Временная метка создания записи (устанавливается автоматически при первом сохранении) */
    @CreationTimestamp
    @Column(name = "created_at",
            nullable = false, updatable = false,
            columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime createdAt;

    /** Временная метка последнего обновления записи (автоматически обновляется при изменении) */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AbstractBaseEntity that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
