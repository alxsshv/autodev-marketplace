package com.autodev.auth.dto.shared;

import java.time.LocalDateTime;

/** DTO для передачи сведений о пользователе.
 * @param id - уникальный идентификатор пользователя,
 * @param keycloakUserId - идентификатор пользователя в keycloak (UUID),
 * @param email - адрес электронной почты,
 * @param enabled - активен ли пользователь,
 * @param createdAt - дата создания аккаунта пользователя.
 * */
public record UserDto(

        Long id,

        String keycloakUserId,

        String email,

        Boolean enabled,

        LocalDateTime createdAt
) {
}
