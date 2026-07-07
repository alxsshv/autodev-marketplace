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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
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
@ActiveProfiles({"docker"})
@EnableWireMock({
        @ConfigureWireMock(name = "keycloak-fo", port = 8301),
        @ConfigureWireMock(name = "platformService-fo", port = 8381, filesUnderClasspath = "wiremock/platform-service"),
})
@TestPropertySource(properties = {
        "spring.application.name=gateway-test-fo",
        "spring.cloud.consul.discovery.enabled=false",
        "spring.cloud.consul.enabled=false",
        "management.endpoint.consul.enabled=false",

        "spring.data.redis.host=localhost",
        "spring.data.redis.port=36379",

        "app.ratelimit.limit-per-ip=2",
        "app.ratelimit.window-duration=1m",

        "spring.security.oauth2.resourceserver.jwt.cache-enabled=false",
        "spring.security.oauth2.resourceserver.jwt.issuer-uri=http://localhost:8301/realms/autodev",

        "logging.level.org.springframework.cloud.gateway=DEBUG",
        "logging.level.org.springframework.data.redis=DEBUG",

        "spring.cloud.gateway.routes[0].id=platform-service",
        "spring.cloud.gateway.routes[0].uri=http://localhost:8381",
        "spring.cloud.gateway.routes[0].predicates[0]=Path=/api/v1/platform/**",
        "spring.cloud.gateway.routes[0].filters[0]=StripPrefix=2"
})
@SuppressWarnings({"java:S2696", "java:S6813"})
class RateLimitingFailOpenIntegrationTest {


    @Autowired
    protected WebTestClient webTestClient;

    @InjectWireMock("keycloak-fo")
    WireMockServer keycloak;

    @InjectWireMock("platformService-fo")
    WireMockServer platformService;

    static final int KEYCLOAK_PORT = 8301;
    static final String KID = "test-kid-1";
    static final String ISSUER = "http://localhost:" + KEYCLOAK_PORT + "/realms/autodev";

    private static RSAKey rsaKey;
    private static String jwt;

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
    @DisplayName("Fail-open: при недоступности Redis запрос проходит успешно")
    void testFailOpenWhenRedisDown() {
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