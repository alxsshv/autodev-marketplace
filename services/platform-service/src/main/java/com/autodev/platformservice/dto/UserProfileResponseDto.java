package com.autodev.platformservice.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * DTO (Data Transfer Object) для передачи данных профиля пользователя.
 * <p>
 * Содержит полную информацию о профиле пользователя, включая:
 * <ul>
 *   <li>Идентификаторы (UUID профиля, Keycloak User ID, email).</li>
 *   <li>Информацию о магазине (название, описание, URL логотипа).</li>
 *   <li>Профильные данные (URL аватара).</li>
 *   <li>Статус верификации и баланс лояльности.</li>
 *   <li>Аудиторские метки (дата создания, дата последнего обновления).</li>
 * </ul>
 * <p>
 * Используется как ответ для:
 * <ul>
 *   <li>GET {@code /api/v1/platform/profile} — получить профиль.</li>
 *   <li>PUT {@code /api/v1/platform/profile} — получить обновлённый профиль.</li>
 * </ul>
 *
 * @see com.autodev.platformservice.controller.UserProfileController
 * @see com.autodev.platformservice.entity.UserProfileEntity
 * @see com.autodev.platformservice.mapper.UserProfileMapper#toDto(com.autodev.platformservice.entity.UserProfileEntity)
 */
public record UserProfileResponseDto(

        /**
         * Уникальный идентификатор записи профиля в локальной БД.
         */
        UUID id,

        /**
         * Адрес электронной почты пользователя.
         */
        String email,

        /**
         * Уникальный идентификатор пользователя в Keycloak.
         */
        String keycloakUserId,

        /**
         * Название магазина пользователя.
         */
        String storeName,

        /**
         * Описание магазина пользователя.
         */
        String storeDescription,

        /**
         * URL-ссылка на логотип магазина.
         */
        String storeLogoUrl,

        /**
         * URL-ссылка на аватар пользователя.
         */
        String avatarUrl,

        /**
         * Статус верификации пользователя.
         * @see com.autodev.platformservice.entity.VerificationStatus
         */
        String verificationStatus,

        /**
         * Баланс лояльности пользователя.
         */
        BigDecimal loyaltyBalance,

        /**
         * Дата и время создания профиля.
         */
        OffsetDateTime createdAt,

        /**
         * Дата и время последнего обновления профиля.
         */
        OffsetDateTime updatedAt

) {
}
