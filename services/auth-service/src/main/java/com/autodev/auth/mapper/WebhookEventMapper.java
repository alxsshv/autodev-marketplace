package com.autodev.auth.mapper;

import com.autodev.auth.dto.shared.WebhookEvent;
import com.autodev.auth.entity.User;
import org.springframework.stereotype.Component;

/**
 * Метод преобразования {@link WebhookEvent} в сущность {@link User}
 */
@Component
public class WebhookEventMapper {

    /** Метод преобразования данных о пользователе из Keycloak в User
     * @param event - событие, генерируемое Keycloak */
    User toEntity(WebhookEvent event) {
        if (event == null || event.user() == null) {
            return null;
        }
        return User.builder()
                .keycloakUserId(event.user().id())
                .email(event.user().email())
                .enabled(event.user().enabled())
                .build();
    }

}
