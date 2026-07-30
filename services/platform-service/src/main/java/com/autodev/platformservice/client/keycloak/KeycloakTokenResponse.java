package com.autodev.platformservice.client.keycloak;

/**
 * Ответ токена от Keycloak.
 * 
 * @param accessToken token для доступа к защищенным ресурсам
 * @param expiresIn время истечения токена в секундах
 * @param tokenType тип токена (обычно "Bearer")
 */
public record KeycloakTokenResponse(
        String accessToken,
        int expiresIn,
        String tokenType
) {
}
