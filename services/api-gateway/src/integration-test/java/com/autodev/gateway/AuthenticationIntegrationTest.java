package com.autodev.gateway;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


class AuthenticationIntegrationTest extends AbstractIntegrationTest {

    @Test
    @DisplayName("Без токена — 401 Unauthorized")
    void testWithoutToken() {
        webTestClient.get()
                .uri("/api/v1/platform/users")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    @DisplayName("Actuator /info должен быть доступен без аутентификации")
    void testHealthEndpoint() {

        webTestClient.get()
                .uri("/actuator/info")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    @DisplayName("С валидным токеном — 200 OK (роутинг работает)")
    void testWithValidToken() {
        webTestClient.get()
                .uri("/api/v1/platform/users")
                .header("Authorization", "Bearer " + jwt)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(1)
                .jsonPath("$.username").isEqualTo("testuser");
    }



}

