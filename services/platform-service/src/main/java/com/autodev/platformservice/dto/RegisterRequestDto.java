package com.autodev.platformservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** DTO для регистрации пользователей */
public record RegisterRequestDto(

        @NotBlank(message = "Адрес электронной посты не может быть пустым")
        @Email(message = "Неверный формат адреса электронной почты")
        @Size(max = 255, message = "Максимальный допустимый размер адреса - 255 символов")
        String email,

        @NotBlank(message = "Пароль пользователя не может быть пустым")
        @Size(min = 8, max = 64, message = "Пароль пользователя должен быть не меньше 6 символов и не более 64 символов")
        @JsonIgnore
        String password,

        @NotBlank(message = "Имя пользователя не может быть пустой")
        @Size(max = 50, message = "Превышена максимально допустимая длина имени пользователя 50 символов")
        String firstName,

        @NotBlank(message = "Фамилия пользователя не может быть пустой")
        @Size(max = 50, message = "Превышена максимально допустимая длина фамилии пользователя 50 символов")
        String lastName


) {
    @Override
    public String toString() {
        return "RegisterRequestDto{" +
                "email='" + email + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                '}';
    }
}
