package com.autodev.gateway;


import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;
import org.wiremock.spring.InjectWireMock;


@SpringBootTest
@AutoConfigureWebTestClient
@TestPropertySource(properties = {
        "spring.application.name=gateway-test",
        "spring.cloud.consul.discovery.service-name=gateway-test",
        "spring.cloud.gateway.discovery.locator.enabled=false",
        "logging.level.org.springframework.cloud.gateway=DEBUG",
        "logging.level.org.springframework.cloud.consul=DEBUG"
})
@ActiveProfiles("docker")
@EnableWireMock({
        @ConfigureWireMock(name = "platformService",            port = 8281, filesUnderClasspath = "wiremock/platform-service"),
        @ConfigureWireMock(name = "orderService",               port = 8282, filesUnderClasspath = "wiremock/order-service"),
        @ConfigureWireMock(name = "consul",                     port = 8283, filesUnderClasspath = "wiremock/consul"),
        @ConfigureWireMock(name = "catalogService",             port = 8284, filesUnderClasspath = "wiremock/catalog-service"),
        @ConfigureWireMock(name = "notificationService",        port = 8285, filesUnderClasspath = "wiremock/notification-service"),
        @ConfigureWireMock(name = "communicationService",       port = 8286, filesUnderClasspath = "wiremock/communication-service"),
        @ConfigureWireMock(name = "paymentService",             port = 8287, filesUnderClasspath = "wiremock/payment-service"),
})
class ApiGatewayRoutingTest {

    @Autowired
    private WebTestClient webTestClient;

    private static final int CONSUL_PORT = 8283;

    @InjectWireMock("platformService")
    WireMockServer platformService;

    @InjectWireMock("orderService")
    WireMockServer orderService;

    @InjectWireMock("consul")
    WireMockServer consul;

    @InjectWireMock("catalogService")
    WireMockServer catalogService;

    @InjectWireMock("communicationService")
    WireMockServer communicationService;

    @InjectWireMock("paymentService")
    WireMockServer paymentService;

    @InjectWireMock("notificationService")
    WireMockServer notificationService;


    @DynamicPropertySource
    static void setConsulProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.cloud.consul.host", () -> "localhost");
        registry.add("spring.cloud.consul.port", () -> CONSUL_PORT);
        registry.add("spring.cloud.consul.discovery.enabled", () -> "true");
    }


    @Test
    @DisplayName("Test platform-service routing")
    void testPlatformServiceRouting() {
        webTestClient.get()
                .uri("/api/v1/platform/users")
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
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.result").isEqualTo("success");
    }




}

