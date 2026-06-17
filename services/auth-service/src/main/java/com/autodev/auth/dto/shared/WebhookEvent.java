package com.autodev.auth.dto.shared;

import java.time.LocalDateTime;

/** DTO для обработки webhook событий от Keycloak.
 * @param event - тип события keycloak,
 * @param timestamp - время наступления события,
 * @param user - Данные пользователя, для которого произошло событие {@link WebhookEvent.User}*/
public record WebhookEvent(

        String event,

        LocalDateTime timestamp,

        User user
) {
    /** DTO для вложенного объекта user в WebhookEvent.
     * @param id - уникальный идентификатор пользователя,
     * @param username - имя пользователя или его адрес электронной почты
     * @param email - адрес электронной почты,
     * @param enabled - активен ли пользователь,
     *  */
    public record User(

            String id,

            String username,

            String email,

            Boolean enabled
    ) {
    }
}
