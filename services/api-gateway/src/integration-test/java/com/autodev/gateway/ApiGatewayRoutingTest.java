package com.autodev.gateway;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


@SuppressWarnings("java:S1192")
class ApiGatewayRoutingTest extends AbstractIntegrationTest {

    @Test
    @DisplayName("Test platform-service routing")
    void testPlatformServiceRouting() {
        webTestClient.get()
                .uri("/api/v1/platform/users")
                .header("Authorization", "Bearer " + jwt)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(1)
                .jsonPath("$.username").isEqualTo("testuser")
                .jsonPath("$.email").isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("Test catalog-service routing")
    void testCatalogServiceRouting() {
        webTestClient.post()
                .uri("/api/v1/catalog")
                .header("Authorization", "Bearer " + jwt)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.result").isEqualTo("success");
    }

    @Test
    @DisplayName("Test order-service routing")
    void testOrderServiceRouting() {
        webTestClient.post()
                .uri("/api/v1/orders")
                .header("Authorization", "Bearer " + jwt)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.result").isEqualTo("success");
    }

    @Test
    @DisplayName("Test payment-service routing")
    void testPaymentServiceRouting() {
        webTestClient.post()
                .uri("/api/v1/payments")
                .header("Authorization", "Bearer " + jwt)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.result").isEqualTo("success");
    }

    @Test
    @DisplayName("Test communication-service routing")
    void testCommunicationServiceRouting() {
        webTestClient.post()
                .uri("/api/v1/communication")
                .header("Authorization", "Bearer " + jwt)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.result").isEqualTo("success");
    }


    @Test
    @DisplayName("Test notification-service routing")
    void testNotificationServiceRouting() {
        webTestClient.post()
                .uri("/api/v1/notifications")
                .header("Authorization", "Bearer " + jwt)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.result").isEqualTo("success");
    }




}

