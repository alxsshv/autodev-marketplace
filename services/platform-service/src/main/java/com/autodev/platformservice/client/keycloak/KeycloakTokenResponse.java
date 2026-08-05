package com.autodev.platformservice.client.keycloak;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Ответ токена от Keycloak.
 * 
 * @param accessToken token для доступа к защищенным ресурсам
 * @param expiresIn время истечения токена в секундах
 * @param tokenType тип токена (обычно "Bearer")
 */
public record KeycloakTokenResponse(
        @JsonProperty("access_token") String accessToken,
        @JsonProperty("expires_in") int expiresIn,
        @JsonProperty("token_type") String tokenType
) {
}
