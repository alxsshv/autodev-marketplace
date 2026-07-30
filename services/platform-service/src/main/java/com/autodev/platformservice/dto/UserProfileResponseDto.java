package com.autodev.platformservice.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * DTO for {@link com.autodev.platformservice.entity.UserProfileEntity}
 */
public record UserProfileResponseDto(
        UUID id,
        String email,
        String keycloakUserId,
        String storeName,
        String storeDescription,
        String storeLogoUrl,
        String avatarUrl,
        String verificationStatus,
        BigDecimal loyaltyBalance,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt) {
}