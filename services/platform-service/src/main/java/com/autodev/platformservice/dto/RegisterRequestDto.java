package com.autodev.platformservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO (Data Transfer Object) для регистрации нового пользователя на платформе.
 * <p>
 * Содержит обязательные данные для создания учётной записи:
 * <ul>
 *   <li>{@code email} — адрес электронной почты (уникальный идентификатор пользователя, до 255 символов).</li>
 *   <li>{@code password} — пароль пользователя (минимум 6, максимум 64 символов).</li>
 *   <li>{@code firstName} — имя пользователя (до 50 символов).</li>
 *   <li>{@code lastName} — фамилия пользователя (до 50 символов).</li>
 * </ul>
 * <p>
 * <b>Валидация:</b> все поля обязательны (аннотация {@code @NotBlank}),
 * email должен соответствовать формату {@code @Email}, пароль — от 6 до 64 символов.
 * <p>
 * <b>Метод {@code toString()}</b> переопределён для безопасности — не включает пароль.
 *
 * @see com.autodev.platformservice.controller.RegistrationController
 * @see com.autodev.platformservice.service.RegistrationService
 */
public record RegisterRequestDto(

        /**
         * Адрес электронной почты пользователя.
         * <p>
         * Обязательный, уникальный идентификатор входа. Формат проверяется
         * аннотацией {@code @Email}. Максимальная длина — 255 символов.
         *
         * @see jakarta.validation.constraints.Email
         * @see jakarta.validation.constraints.NotBlank
         * @see jakarta.validation.constraints.Size
         */
        @NotBlank(message = "Адрес электронной почты не может быть пустым")
        @Email(message = "Неверный формат адреса электронной почты")
        @Size(max = 255, message = "Максимальный допустимый размер адреса - 255 символов")
        String email,

        /**
         * Пароль пользователя для аутентификации в Keycloak.
         * <p>
         * Минимальная длина — 6 символов, максимальная — 64 символа.
         * <b>Не сериализуется в JSON и не выводится в логах</b>
         * (аннотация {@code @JsonIgnore}).
         *
         * @see jakarta.validation.constraints.NotBlank
         * @see jakarta.validation.constraints.Size
         */
        @NotBlank(message = "Пароль пользователя не может быть пустым")
        @Size(min = 8, max = 64, message = "Пароль пользователя должен быть не меньше 6 символов и не более 64 символов")
        String password,

        /**
         * Имя пользователя.
         * <p>
         * Обязательное поле. Максимальная длина — 50 символов.
         *
         * @see jakarta.validation.constraints.NotBlank
         * @see jakarta.validation.constraints.Size
         */
        @NotBlank(message = "Имя пользователя не может быть пустым")
        @Size(max = 50, message = "Превышена максимально допустимая длина имени пользователя (50 символов)")
        String firstName,

        /**
         * Фамилия пользователя.
         * <p>
         * Обязательное поле. Максимальная длина — 50 символов.
         *
         * @see jakarta.validation.constraints.NotBlank
         * @see jakarta.validation.constraints.Size
         */
        @NotBlank(message = "Фамилия пользователя не может быть пустой")
        @Size(max = 50, message = "Превышена максимально допустимая длина фамилии пользователя (50 символов)")
        String lastName

) {
    /**
     * Возвращает строковое представление DTO без пароля.
     * <p>
     * Переопределяет стандартный {@code toString()} для безопасности:
     * исключает {@code password} из выводимых данных.
     *
     * @return строковое представление без пароля
     */
    @Override
    public String toString() {
        return "RegisterRequestDto{" +
                "email='" + email + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                '}';
    }
}
