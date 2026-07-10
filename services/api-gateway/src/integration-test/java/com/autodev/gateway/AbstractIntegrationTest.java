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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

@SpringBootTest
@AutoConfigureWebTestClient
@TestPropertySource(properties = {
        "spring.application.name=gateway-test",
        "spring.cloud.consul.discovery.service-name=gateway-test",
        "logging.level.org.springframework.cloud.gateway=DEBUG",
        "logging.level.org.springframework.cloud.consul=DEBUG",
        "spring.security.oauth2.resourceserver.jwt.cache-enabled=false",
        "cors.allowed-origins=http://localhost:3000",
        "spring.webflux.cors.enabled=false"
})
@ActiveProfiles({"docker"})
@EnableWireMock({
        @ConfigureWireMock(name = "keycloak", port = 8201),
        @ConfigureWireMock(name = "platformService", port = 8281, filesUnderClasspath = "wiremock/platform-service"),
        @ConfigureWireMock(name = "orderService", port = 8282, filesUnderClasspath = "wiremock/order-service"),
        @ConfigureWireMock(name = "consul", port = 8283, filesUnderClasspath = "wiremock/consul"),
        @ConfigureWireMock(name = "catalogService", port = 8284, filesUnderClasspath = "wiremock/catalog-service"),
        @ConfigureWireMock(name = "notificationService", port = 8285, filesUnderClasspath = "wiremock/notification-service"),
        @ConfigureWireMock(name = "communicationService", port = 8286, filesUnderClasspath = "wiremock/communication-service"),
        @ConfigureWireMock(name = "paymentService", port = 8287, filesUnderClasspath = "wiremock/payment-service"),
})
@Testcontainers
@SuppressWarnings({"java:S2696", "java:S6813"})
public abstract class AbstractIntegrationTest {

    private static final Logger log = LoggerFactory.getLogger(AbstractIntegrationTest.class);
    static final int CONSUL_PORT = 8283;
    static final int KEYCLOAK_PORT = 8201;

    static final String KID = "test-kid-1";
    static final String ISSUER = "http://localhost:" + KEYCLOAK_PORT + "/realms/autodev";

    @Autowired
    protected WebTestClient webTestClient;

    @Autowired
    protected RedisTemplate<String, String> redisTemplate;

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

    @InjectWireMock("keycloak")
    WireMockServer keycloak;

    static {
        System.setProperty("REDIS_HOST", TestRedisContainer.getHost());
        System.setProperty("REDIS_PORT", TestRedisContainer.getPort().toString());
    }


    @DynamicPropertySource
    static void setConsulProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.cloud.consul.host", () -> "localhost");
        registry.add("spring.cloud.consul.port", () -> CONSUL_PORT);
        registry.add("spring.cloud.consul.discovery.enabled", () -> "true");
        registry.add("management.endpoint.health.consul.enabled", () -> false);
        registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri", () -> ISSUER);
    }

    static RSAKey rsaKey;
    static String jwt;

    @BeforeEach
    void setUp() throws JOSEException {
        log.info("============= check token ============");
        if (jwt == null) {
            log.info("============= generate token ============");
            rsaKey = new RSAKeyGenerator(2048).keyID(KID).generate();
            PrivateKey privateKey = rsaKey.toRSAPrivateKey();
            jwt = generateValidJwt(privateKey);
        }

        log.info("============= check stubMappings ============");
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

        // Исправленная очистка Redis: используем sync().flushdb() вместо устаревшего flushDb()
        redisTemplate.execute((RedisConnection connection) -> {
            connection.serverCommands().flushDb();
            return "OK";
        });
        log.info("Redis flushed (FLUSHDB) before test execution.");
    }

    /**
     * Генерация JWT с тем же приватным ключом
     */
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


}
