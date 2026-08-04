package com.autodev.platformservice.dto.event;

public record UserRegisteredEventPayload(
        String keycloakUserId,
        String email,
        String firstName,
        String lastName) {
}
