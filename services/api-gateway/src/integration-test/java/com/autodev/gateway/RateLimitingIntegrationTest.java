package com.autodev.gateway;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;
import org.wiremock.spring.InjectWireMock;

import java.security.PrivateKey;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureWebTestClient
@ActiveProfiles({"docker"})
@EnableWireMock({
        @ConfigureWireMock(name = "keycloak-rl", port = 8401),
        @ConfigureWireMock(name = "platformService-rl", port = 8481, filesUnderClasspath = "wiremock/platform-service"),
        @ConfigureWireMock(name = "consul-rl", port = 8483, filesUnderClasspath = "wiremock/consul"),
})
@TestPropertySource(properties = {
        "spring.application.name=gateway-test-rl",
        "spring.cloud.consul.discovery.service-name=gateway-test-rl",

        "app.ratelimit.limit-per-ip=2",
        "app.ratelimit.window-duration=1m",

        "logging.level.org.springframework.cloud.gateway=DEBUG",
        "logging.level.org.springframework.cloud.consul=DEBUG",
        "logging.level.com.autodev.gateway.filter=DEBUG",
        "spring.security.oauth2.resourceserver.jwt.cache-enabled=false"
})
@Testcontainers
@SuppressWarnings({"java:S2696", "java:S6813", "java:S1192", "java:S1313", "java:S1075"})
class RateLimitingIntegrationTest {

    static final int CONSUL_PORT = 8483;
    static final int KEYCLOAK_PORT = 8401;

    static final String KID = "test-kid-1";
    static final String ISSUER = "http://localhost:" + KEYCLOAK_PORT + "/realms/autodev";

    static final String PATH = "/api/v1/platform/users";

    @Autowired
    protected WebTestClient webTestClient;

    @Autowired
    protected RedisTemplate<String, String> redisTemplate;

    @InjectWireMock("platformService-rl")
    WireMockServer platformService;

    @InjectWireMock("consul-rl")
    WireMockServer consul;

    @InjectWireMock("keycloak-rl")
    WireMockServer keycloak;

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.cloud.consul.host", () -> "localhost");
        registry.add("spring.cloud.consul.port", () -> CONSUL_PORT);
        registry.add("spring.cloud.consul.discovery.enabled", () -> "true");
        registry.add("management.endpoint.health.consul.enabled", () -> false);
        registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri", () -> ISSUER);
        registry.add("spring.data.redis.host", TestRedisContainer::getHost);
        registry.add("spring.data.redis.port", TestRedisContainer::getPort);
    }

    static RSAKey rsaKey;
    static String jwt;

    @BeforeEach
    void setUp() throws JOSEException {
        if (jwt == null) {
            rsaKey = new RSAKeyGenerator(2048).keyID(KID).generate();
            PrivateKey privateKey = rsaKey.toRSAPrivateKey();
            jwt = generateValidJwt(privateKey);
        }

        if (keycloak.getStubMappings().isEmpty()) {
            String baseUrl = "http://localhost:" + KEYCLOAK_PORT;
            String openidConfig = """
                    {
                      "issuer": "%s",
                      "jwks_uri": "%s/realms/autodev/protocol/openid-connect/certs"
                    }
                    """.formatted(ISSUER, baseUrl);

            keycloak.stubFor(get(urlEqualTo("/realms/autodev/.well-known/openid-configuration"))
                    .willReturn(aResponse()
                            .withHeader("Content-Type", "application/json")
                            .withBody(openidConfig)));

            String jwks = rsaKey.toPublicJWK().toJSONString();
            String jwksSet = "{\"keys\":[" + jwks + "]}";

            keycloak.stubFor(get(urlEqualTo("/realms/autodev/protocol/openid-connect/certs"))
                    .willReturn(aResponse()
                            .withHeader("Content-Type", "application/json")
                            .withBody(jwksSet)));
        }

        redisTemplate.execute((RedisConnection connection) -> {
            connection.serverCommands().flushDb();
            return "OK";
        });
    }

    private String generateValidJwt(PrivateKey privateKey) throws JOSEException {
        long oneHourInMillis = 3600000L;
        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject("test-user")
                .issuer(ISSUER)
                .audience("account")
                .claim("realm_access", Map.of(
                        "roles", List.of("ROLE_ADMIN", "ROLE_USER"),
                        "realm", "autodev"
                ))
                .expirationTime(new Date(System.currentTimeMillis() + oneHourInMillis))
                .build();

        JWSHeader header = new JWSHeader.Builder(com.nimbusds.jose.JWSAlgorithm.RS256)
                .keyID(KID)
                .build();

        JWSSigner signer = new RSASSASigner(privateKey);
        SignedJWT signedJWT = new SignedJWT(header, claimsSet);
        signedJWT.sign(signer);

        return signedJWT.serialize();
    }

    @Test
    @DisplayName("Превышение лимита запросов — 429 Too Many Requests с Retry-After")
    void testRateLimitExceeded() {
        String ip = "10.0.0.1";

        webTestClient.get().uri(PATH)
                .header("Authorization", "Bearer " + jwt)
                .header("X-Forwarded-For", ip)
                .exchange()
                .expectStatus().isOk();

        webTestClient.get().uri(PATH)
                .header("Authorization", "Bearer " + jwt)
                .header("X-Forwarded-For", ip)
                .exchange()
                .expectStatus().isOk();

        webTestClient.get().uri(PATH)
                .header("Authorization", "Bearer " + jwt)
                .header("X-Forwarded-For", ip)
                .exchange()
                .expectStatus().isEqualTo(429)
                .expectHeader().exists("Retry-After");
    }

    @Test
    @DisplayName("Исключённый путь /actuator не лимитируется")
    @SuppressWarnings("java:S5960")
    void testExcludedPath() {
        for (int i = 0; i < 5; i++) {
            webTestClient.get().uri("/actuator/health")
                    .exchange()
                    .expectStatus().value(rawStatus -> assertThat(rawStatus).isNotEqualTo(429));
        }
    }

    @Test
    @DisplayName("Разные IP имеют независимые счётчики лимитов")
    void testDifferentIps() {
        for (int i = 0; i < 2; i++) {
            webTestClient.get().uri(PATH)
                    .header("Authorization", "Bearer " + jwt)
                    .header("X-Forwarded-For", "10.0.0.1")
                    .exchange()
                    .expectStatus().isOk();
        }

        webTestClient.get().uri(PATH)
                .header("Authorization", "Bearer " + jwt)
                .header("X-Forwarded-For", "10.0.0.1")
                .exchange()
                .expectStatus().isEqualTo(429);

        for (int i = 0; i < 2; i++) {
            webTestClient.get().uri(PATH)
                    .header("Authorization", "Bearer " + jwt)
                    .header("X-Forwarded-For", "10.0.0.2")
                    .exchange()
                    .expectStatus().isOk();
        }

        webTestClient.get().uri(PATH)
                .header("Authorization", "Bearer " + jwt)
                .header("X-Forwarded-For", "10.0.0.2")
                .exchange()
                .expectStatus().isEqualTo(429);
    }

    @Test
    @DisplayName("IP из заголовка X-Forwarded-For: берётся последний IP из цепочки")
    void testXForwardedForLastIp() {

        webTestClient.get().uri(PATH)
                .header("Authorization", "Bearer " + jwt)
                .header("X-Forwarded-For", "10.0.0.1, 10.0.0.2, 10.0.0.3")
                .exchange()
                .expectStatus().isOk();

        webTestClient.get().uri(PATH)
                .header("Authorization", "Bearer " + jwt)
                .header("X-Forwarded-For", "10.0.0.1, 10.0.0.2, 10.0.0.3")
                .exchange()
                .expectStatus().isOk();

        webTestClient.get().uri(PATH)
                .header("Authorization", "Bearer " + jwt)
                .header("X-Forwarded-For", "10.0.0.1, 10.0.0.2, 10.0.0.3")
                .exchange()
                .expectStatus().isEqualTo(429);
    }
}