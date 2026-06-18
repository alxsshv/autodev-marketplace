package com.autodev.auth.mapper;

import com.autodev.auth.dto.shared.Event;
import com.autodev.auth.dto.shared.WebhookEvent;
import com.autodev.auth.entity.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

class WebhookEventMapperTest {

    private final WebhookEventMapper mapper = new WebhookEventMapper();

    @Test
    @DisplayName("Test toEntity method with valid WebhookEvent should return valid user")
    void toEntity_withValidWebhookEvent_shouldReturnValidUser() {
        WebhookEvent event = new WebhookEvent(
                Event.USER_CREATED.toString().toLowerCase(),
                LocalDateTime.now(),
                new WebhookEvent.User(
                        UUID.randomUUID().toString(),
                        "username",
                        "user@email.com",
                        true));

        User user = mapper.toEntity(event);

        Assertions.assertAll("Checking user",
                () -> Assertions.assertEquals(event.user().email(), user.getEmail()),
                () -> Assertions.assertEquals(event.user().enabled(), user.isEnabled()),
                () -> Assertions.assertEquals(event.user().id(), user.getKeycloakUserId())
        );
    }

    @Test
    @DisplayName("Test toEntity method with WebhookEvent is null should return null")
    void toEntity_withWebhookEventIsNull_shouldReturnNull() {
        User user = mapper.toEntity(null);

        Assertions.assertNull(user);
    }

    @Test
    @DisplayName("Test toEntity method with WebhookEvents user is null should return null")
    void toEntity_withWebhookEventsUserIsNull_shouldReturnNull() {
        WebhookEvent event = new WebhookEvent(
                Event.USER_CREATED.toString().toLowerCase(),
                LocalDateTime.now(),
                null);

        User user = mapper.toEntity(event);

        Assertions.assertNull(user);
    }
}
