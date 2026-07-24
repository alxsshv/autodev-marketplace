package com.autodev.platformservice.repository;

import com.autodev.platformservice.entity.OutboxEntity;
import com.autodev.platformservice.entity.OutboxStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OutboxRepository extends JpaRepository<OutboxEntity, UUID> {

    List<OutboxEntity> findByStatusOrderByCreatedAtAsc(OutboxStatus status, Pageable pageable);

}
