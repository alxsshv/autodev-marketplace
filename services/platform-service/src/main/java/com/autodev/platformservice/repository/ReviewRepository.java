package com.autodev.platformservice.repository;

import com.autodev.platformservice.entity.ReviewEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ReviewRepository extends JpaRepository<ReviewEntity, UUID> {

    List<ReviewEntity> findByProductIdOrderByCreatedAtDesc(String productId);

    List<ReviewEntity> findByUserId(String userId);

}
