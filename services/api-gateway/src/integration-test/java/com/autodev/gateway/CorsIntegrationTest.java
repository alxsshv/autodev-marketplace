package com.autodev.gateway;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CORS Integration Tests")
@SuppressWarnings({"java:S100", "java:S2259", "java:S1192", "java:S5960"})
class CorsIntegrationTest extends AbstractIntegrationTest {

    private static final String TRUSTED_ORIGIN = "http://localhost:3000";
    private static final String UNTRUSTED_ORIGIN = "http://evil.com";

    @Test
    @DisplayName("Preflight OPTIONS должен возвращать 200 и корректные CORS-заголовки")
    void preflightOptions_shouldReturn200AndValidCorsHeaders() {
        final List<String> allowedMethods = Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH");
        final List<String> allowedHeaders = Arrays.asList(
                "Authorization", "Content-Type", "X-Request-ID", "X-Language"
        );

        var result = webTestClient
                .options().uri("/api/v1/orders")
                .header("Origin", TRUSTED_ORIGIN)
                .header("Access-Control-Request-Method", "GET")
                .header("Access-Control-Request-Headers", "Authorization,Content-Type")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().exists("Access-Control-Allow-Origin")
                .expectHeader().exists("Access-Control-Allow-Methods")
                .expectHeader().exists("Access-Control-Allow-Headers")
                .expectHeader().exists("Access-Control-Max-Age")
                .expectBody().returnResult();

        HttpHeaders headers = result.getResponseHeaders();

        assertThat(headers.getFirst("Access-Control-Allow-Origin"))
                .isEqualTo(TRUSTED_ORIGIN);

        String methodsHeader = headers.getFirst("Access-Control-Allow-Methods");
        assertThat(methodsHeader).isNotNull();
        List<String> methodsList = Arrays.stream(methodsHeader.split(","))
                .map(String::trim)
                .toList();
        assertThat(methodsList).containsAll(allowedMethods);

        String headersHeader = headers.getFirst("Access-Control-Allow-Headers");
        assertThat(headersHeader).isNotNull();
        List<String> headersList = Arrays.stream(headersHeader.split(","))
                .map(String::trim)
                .toList();
        assertThat(headersList).containsAll(allowedHeaders);
    }

    @Test
    @DisplayName("Обычный GET с доверенным Origin должен возвращать Access-Control-Allow-Origin (не *)")
    void regularGet_withTrustedOrigin_shouldReturnValidCorsHeader() {
        var result = webTestClient
                .get().uri("/api/v1/orders")
                .header("Origin", TRUSTED_ORIGIN)
                .header("Authorization", "Bearer " + jwt)
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectHeader().exists("Access-Control-Allow-Origin")
                .expectBody().returnResult();

        String allowOrigin = result.getResponseHeaders().getFirst("Access-Control-Allow-Origin");
        assertThat(allowOrigin).isEqualTo(TRUSTED_ORIGIN);
    }

    @Test
    @DisplayName("Запрос с недоверенным Origin: CORS-заголовок не должен быть wildcard '*'")
    void request_withUntrustedOrigin_shouldNotAllowWildcardCors() {
        var result = webTestClient
                .get().uri("/api/v1/orders")
                .header("Origin", UNTRUSTED_ORIGIN)
                .header("Authorization", "Bearer " + jwt)
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody().returnResult();

        String allowOrigin = result.getResponseHeaders().getFirst("Access-Control-Allow-Origin");

        // Если заголовок есть — он не должен быть '*'. Если его нет — это тоже ок.
        if (allowOrigin != null) {
            assertThat(allowOrigin).isNotEqualTo("*");
        }
    }
}
