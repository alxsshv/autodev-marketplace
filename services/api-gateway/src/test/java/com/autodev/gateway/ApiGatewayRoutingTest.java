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
        @ConfigureWireMock(name = "userService",                port = 8281, filesUnderClasspath = "wiremock/user-service"),
        @ConfigureWireMock(name = "authService",                port = 8282, filesUnderClasspath = "wiremock/auth-service"),
        @ConfigureWireMock(name = "consul",                     port = 8283, filesUnderClasspath = "wiremock/consul"),
        @ConfigureWireMock(name = "catalogService",             port = 8284, filesUnderClasspath = "wiremock/catalog-service"),
        @ConfigureWireMock(name = "pricingInventoryService",    port = 8285, filesUnderClasspath = "wiremock/pricing-inventory-service"),
        @ConfigureWireMock(name = "searchService",              port = 8286, filesUnderClasspath = "wiremock/search-service"),
        @ConfigureWireMock(name = "orderService",               port = 8287, filesUnderClasspath = "wiremock/order-service"),
        @ConfigureWireMock(name = "notificationService",        port = 8288, filesUnderClasspath = "wiremock/notification-service"),
        @ConfigureWireMock(name = "reviewService",              port = 8289, filesUnderClasspath = "wiremock/review-service"),
        @ConfigureWireMock(name = "recommendationService",      port = 8290, filesUnderClasspath = "wiremock/recommendation-service"),
        @ConfigureWireMock(name = "adminService",               port = 8291, filesUnderClasspath = "wiremock/admin-service"),
        @ConfigureWireMock(name = "analyticsService",           port = 8292, filesUnderClasspath = "wiremock/analytics-service"),
})
class ApiGatewayRoutingTest {

    @Autowired
    private WebTestClient webTestClient;

    private static final int CONSUL_PORT = 8283;

    @InjectWireMock("userService")
    WireMockServer userService;

    @InjectWireMock("authService")
    WireMockServer authService;

    @InjectWireMock("consul")
    WireMockServer consul;

    @InjectWireMock("catalogService")
    WireMockServer catalogService;

    @InjectWireMock("pricingInventoryService")
    WireMockServer pricingInventoryService;

    @InjectWireMock("searchService")
    WireMockServer searchService;

    @InjectWireMock("orderService")
    WireMockServer orderService;

    @InjectWireMock("notificationService")
    WireMockServer notificationService;

    @InjectWireMock("reviewService")
    WireMockServer reviewService;

    @InjectWireMock("recommendationService")
    WireMockServer recommendationService;

    @InjectWireMock("adminService")
    WireMockServer adminService;

    @InjectWireMock("analyticsService")
    WireMockServer analyticsService;


    @DynamicPropertySource
    static void setConsulProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.cloud.consul.host", () -> "localhost");
        registry.add("spring.cloud.consul.port", () -> CONSUL_PORT);
        registry.add("spring.cloud.consul.discovery.enabled", () -> "true");
    }


    @Test
    @DisplayName("Test user-service routing")
    void testUserServiceRouting() {
        webTestClient.get()
                .uri("/api/v1/users")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(1)
                .jsonPath("$.username").isEqualTo("testuser")
                .jsonPath("$.email").isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("Test auth-service routing")
    void testAuthServiceRouting() {
        webTestClient.post()
                .uri("/api/v1/auth/login")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.token").isEqualTo("test-token")
                .jsonPath("$.expiresIn").isEqualTo(3600);
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
    @DisplayName("Test pricing-inventory-service routing")
    void testPricingInventoryServiceRouting() {
        webTestClient.post()
                .uri("/api/v1/price")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.result").isEqualTo("success");
    }

    @Test
    @DisplayName("Test search-service routing")
    void testSearchServiceRouting() {
        webTestClient.post()
                .uri("/api/v1/search")
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
    @DisplayName("Test notification-service routing")
    void testNotificationServiceRouting() {
        webTestClient.post()
                .uri("/api/v1/notifications")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.result").isEqualTo("success");
    }

    @Test
    @DisplayName("Test review-service routing")
    void testReviewServiceRouting() {
        webTestClient.post()
                .uri("/api/v1/reviews")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.result").isEqualTo("success");
    }

    @Test
    @DisplayName("Test recommendation-service routing")
    void testRecommendationServiceRouting() {
        webTestClient.post()
                .uri("/api/v1/recommendations")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.result").isEqualTo("success");
    }

    @Test
    @DisplayName("Test admin-service routing")
    void testAdminServiceRouting() {
        webTestClient.post()
                .uri("/api/v1/admin")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.result").isEqualTo("success");
    }

    @Test
    @DisplayName("Test analytics-service routing")
    void testAnalyticsServiceRouting() {
        webTestClient.post()
                .uri("/api/v1/analytics")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.result").isEqualTo("success");
    }


}

