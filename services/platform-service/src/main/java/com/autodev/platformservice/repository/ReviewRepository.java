package com.autodev.platformservice.repository;

import com.autodev.platformservice.entity.ReviewEntity;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ReviewRepository {

    List<ReviewEntity> findByProductidOrderByCreatedAtDesc(UUID productId);

    List<ReviewEntity> findByUserId(UUID userId);

}
