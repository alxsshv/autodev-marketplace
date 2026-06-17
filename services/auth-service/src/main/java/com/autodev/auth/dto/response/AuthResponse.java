package com.autodev.auth.dto.response;

/** Ответ сервиса на положительный результат операции обновления токена.
 * @param accessToken - токен, по которому осуществляется аутентификация пользователя,
 * @param tokenType - тип access токена (например: Bearer),
 * @param expiresIn - время протухания токена,
 * @param refreshToken - токен для обновления access и refresh токена.
 */
public record AuthResponse(
        String accessToken,
        String tokenType,
        Integer expiresIn,
        String refreshToken
) {
}
