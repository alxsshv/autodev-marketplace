package com.autodev.platformservice.client.keycloak;

import com.autodev.platformservice.config.KeycloakProperties;
import com.autodev.platformservice.exception.KeycloakInfrastructureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;

/**
 * Провайдер токенов Keycloak для сервисного аккаунта.
 * <p>
 * Обеспечивает получение, кэширование и автоматическое обновление access token'ов
 * для взаимодействия с защищенными ресурсами Keycloak. Использует паттерн
 * Double-Checked Locking для оптимизации производительности при многопоточном доступе.
 */
@Component
@Slf4j
public class KeycloakTokenProvider {

    private static final int SAFETY_MARGIN_SECONDS = 10;

    private final KeycloakProperties keycloakAdminProps;

    private final RestClient restClient;

    /** Кэш для токена */
    private CachedToken cachedToken;


    /**
     * Создает новый экземпляр провайдера токенов.
     *
     * @param keycloakAdminProps конфигурационные свойства Keycloak
     */
    public KeycloakTokenProvider(KeycloakProperties keycloakAdminProps) {
        this.keycloakAdminProps = keycloakAdminProps;
        this.restClient = RestClient.builder()
                .baseUrl(keycloakAdminProps.serverUrl())
                .build();
    }


    /**
     * Получает токен для сервисного аккаунта.
     * <p>
     * Использует Double-Checked Locking для максимальной производительности:
     * 99% запросов отработают БЕЗ блокировки (когда токен в кэше).
     *
     * @return access token в виде строки
     * @throws KeycloakInfrastructureException если произошла ошибка получения токена
     */
    public String getServiceClientAccessToken() {
        if (cachedToken != null && cachedToken.expiredAt != null && cachedToken.isNotExpired()) {
            return cachedToken.token;
        }
        synchronized (this) {
            if (cachedToken != null && cachedToken.expiredAt != null && cachedToken.isNotExpired()) {
                return cachedToken.token;
            }
            log.info("Keycloak service account token is missing or expired. Fetching a new one...");
            KeycloakTokenResponse response = getNewToken();
            cachedToken = new CachedToken(
                    response.accessToken(),
                    Instant.now().plusSeconds(response.expiresIn() - SAFETY_MARGIN_SECONDS));
            return cachedToken.token;
        }
    }

    private KeycloakTokenResponse getNewToken() {
        String credentials = keycloakAdminProps.clientId() + ":" + keycloakAdminProps.clientSecret();
        String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
        String requestBody = "grant_type=client_credentials";

        try {
            KeycloakTokenResponse response = restClient.post()
                    .uri("/realms/{realm}/protocol/openid-connect/token", keycloakAdminProps.realm())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .header("Authorization", "Basic " + encodedCredentials)
                    .body(requestBody)
                    .retrieve()
                    .body(KeycloakTokenResponse.class);
            if (response == null || response.accessToken() == null) {
                log.error("Keycloak returned 200 OK, but token body is null or malformed");
                throw new KeycloakInfrastructureException("Ошибка сервиса авторизации. Попробуйте позже");
            }
            return response;
        } catch (ResourceAccessException ex) {
            // Connection refused, timeout — Keycloak физически не доступен
            log.error("Keycloak is unavailable: {}", ex.getMessage());
            throw new KeycloakInfrastructureException("Сервис авторизации временно недоступен. Попробуйте позже.");

        } catch (RestClientResponseException ex) {
            // Keycloak ответил, но с ошибкой (например, 401 Unauthorized — неверный client_secret)
            log.error("Keycloak returned error status: {}, body: {}", ex.getStatusCode(), ex.getResponseBodyAsString());
            throw new KeycloakInfrastructureException("Ошибка конфигурации сервиса авторизации.");
        }

    }

    /**
     * Кэшированный токен с датой истечения срока действия.
     *
     * @param token    access token
     * @param expiredAt время истечения срока действия токена
     */
    private record CachedToken(
            String token,
            Instant expiredAt
    ) {
        /**
         * Проверяет, что токен еще не истек.
         *
         * @return {@code true}, если токен действителен, иначе {@code false}
         */
        boolean isNotExpired() {
            return Instant.now().isBefore(expiredAt);
        }
    }
}
