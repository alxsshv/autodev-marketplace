package com.autodev.auth.dto.request;


import jakarta.validation.constraints.NotBlank;

/** DTO для аутентификации по email и паролю.
 * @param email - адрес электронной почты пользователя.
 * @param password - пароль пользователя. */
public record LoginRequest(

        @NotBlank
        String email,

        @NotBlank
        String password
) {}
