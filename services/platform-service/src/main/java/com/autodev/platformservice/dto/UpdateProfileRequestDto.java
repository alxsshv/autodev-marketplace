package com.autodev.platformservice.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO (Data Transfer Object) для обновления профиля пользователя.
 * <p>
 * Содержит неклинические данные профиля магазина:
 * <ul>
 *   <li>{@code storeName} — название магазина (до 255 символов).</li>
 *   <li>{@code storeDescription} — описание магазина (до 2000 символов).</li>
 *   <li>{@code phone} — номер телефона (7–15 символов, форматы: {@code +7-XXX-XXX-XX-XX}, {@code 8XXX...}, etc.).</li>
 * </ul>
 * <p>
 * <b>Необновляемые поля:</b> email, ключевые поля в Keycloak, баланс лояльности,
 * статус верификации, аудиторские данные — эти данные не доступны для изменения
 * через этот DTO.
 * <p>
 * <b>Валидация:</b> все поля опциональные (без {@code @NotBlank}), но при
 * наличии значения — проверяются максимальные размеры и формат телефона.
 *
 * @see com.autodev.platformservice.controller.UserProfileController
 * @see com.autodev.platformservice.service.UserProfileService
 */
public record UpdateProfileRequestDto(

        @Size(max = 255, message = "Превышена максимально допустимая длина названия магазина (255 символов)")
        String storeName,

        @Size(max = 2000, message = "Максимальная длина описания магазина не должна превышать 2000 символов")
        String storeDescription,

        @Pattern(regexp = "^\\+?[0-9\\-\\s]{7,15}$", message = "Неверный формат номера телефона")
        @Size(max = 20, message = "Превышена максимально допустимая длина номера телефона (20 символов)")
        String phone,

        @Size(max = 255, message = "Превышена максимально допустимая длина URL для логотипа магазина (255 символов)")
        String storeLogoUrl,

        @Size(max = 255, message = "Превышена максимально допустимая длина URL для логотипа пользователя (255 символов)")
        String avatarUrl
) {
}
