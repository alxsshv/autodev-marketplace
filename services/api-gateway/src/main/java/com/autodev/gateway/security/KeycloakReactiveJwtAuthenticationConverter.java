package com.autodev.gateway.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.Nullable;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Конвертер JWT-токенов Keycloak в Spring Security authentication token.
 * <p>
 * Извлекает роли пользователя из claim 'realm_access' токена Keycloak и преобразует
 * их в коллекцию Spring Security {@link GrantedAuthority}. Предполагается, что
 * Keycloak уже возвращает роли с префиксом 'ROLE_'.
 * </p>
 * <p>
 * Реализует паттерн Reactive Converter для интеграции с WebFlux-приложениями.
 * </p>
 *
 * @author Autodev
 * @see Converter
 * @see Jwt
 * @see AbstractAuthenticationToken
 * @see JwtAuthenticationToken
 * @since 1.0
 */
@SuppressWarnings("java:S2638")
@Component
public class KeycloakReactiveJwtAuthenticationConverter implements Converter<Jwt, Mono<AbstractAuthenticationToken>> {

    private static final String AUTHORITY_CLAIM_NAME = "realm_access";

    /**
     * Конвертирует JWT токен в Spring Security authentication token.
     * <p>
     * Если JWT токен null, возвращает пустой Mono. Иначе извлекает Authorities
     * из токена и создаёт новый JwtAuthenticationToken.
     * </p>
     *
     * @param jwt JWT токен для конвертации, может быть null
     * @return Mono с AbstractAuthenticationToken или пустой Mono если jwt null
     * @see JwtAuthenticationToken
     * @see #extractAuthorities(Jwt)
     */
    @Override
    public Mono<AbstractAuthenticationToken> convert(@Nullable Jwt jwt) {
        if (jwt == null) {
            return Mono.empty();
        }
        Collection<GrantedAuthority> authorities = extractAuthorities(jwt);
        var token = new JwtAuthenticationToken(jwt, authorities);
        token.setAuthenticated(true);
        return Mono.just(token);
    }

    /**
     * Извлекает Authorities (роли) из JWT токена.
     * <p>
     * Получает claim 'realm_access' из токена, извлекает список ролей и
     * преобразует их в SimpleGrantedAuthority.
     * </p>
     *
     * @param jwt JWT токен для извлечения authorities
     * @return Коллекция GrantedAuthority для токена, не null
     * @see #getClaim(Jwt, String)
     * @see SimpleGrantedAuthority
     */
    private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
        Map<String, Object> realmAccess = getClaim(jwt, AUTHORITY_CLAIM_NAME);
        if (realmAccess == null) {
            return List.of();
        }

        @SuppressWarnings("unchecked")
        List<String> roles = (List<String>) realmAccess.get("roles");
        if (roles == null || roles.isEmpty()) {
            return List.of();
        }

        // Keycloak уже отдаёт роли с префиксом ROLE_, поэтому передаём как есть
        return roles.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    /**
     * Получает claim из JWT токена по имени claim.
     *
     * @param <T>       тип возвращаемого значения
     * @param jwt       JWT токен
     * @param claimName имя claim для извлечения
     * @return значение claim или null если claim не найден
     */
    @SuppressWarnings("unchecked")
    private <T> T getClaim(Jwt jwt, String claimName) {
        return (T) jwt.getClaims().get(claimName);
    }
}
