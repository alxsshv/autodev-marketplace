package com.autodev.platformservice.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Конвертер JWT-токена Keycloak в коллекцию полномочий (GrantedAuthority) для Spring Security.
 * <p>
 * Реализует интерфейс {@link Converter}, который используется Spring Security
 * для извлечения ролей из JWT-токена и преобразования их в авторизационные
 * полномочия, понятные фреймворку.
 * <p>
 * <b>Формат JWT Keycloak:</b>
 * <pre>{@code
 * {
 *   "realm_access": {
 *     "roles": ["ROLE_ADMIN", "ROLE_USER", "ROLE_MODERATOR"]
 *   },
 *   "resource_access": { ... },
 *   ...
 * }
 * }</pre>
 * <p>
 * Конвертер извлекает список ролей из вложенного объекта {@code realm_access.roles}
 * и преобразует каждую роль в {@code SimpleGrantedAuthority} с префиксом {@code ROLE_},
 * что соответствует约定 Spring Security.
 * <p>
 * <b>Пример:</b> если Keycloak возвращает роль {@code "ADMIN"}, конвертер преобразует
 * её в {@code SimpleGrantedAuthority("ROLE_ADMIN")}.
 *
 * @see org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer
 * @see org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter
 */
@Slf4j
public class KeycloakJwtAuthenticationConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    /**
     * Имя клаима в JWT-токене, содержащего объект с ролями realm.
     * <p>
     * В Keycloak JWT-токен имеет структуру {@code {"realm_access": {"roles": [...]}}},
     * и это значение соответствует ключу {@code realm_access}.
     */
    private static final String ROLES_CLAIM_NAME = "realm_access";

    /**
     * Имя ключа внутри клаима {@code realm_access}, содержащего список ролей.
     * <p>
     * В Keycloak JWT-токене роли находятся по пути {@code realm_access.roles}.
     */
    private static final String ROLES_KEY = "roles";

    /**
     * Преобразует JWT-токен в коллекцию полномочий Spring Security.
     * <p>
     * <b>Алгоритм:</b>
     * <ol>
     *   <li>Извлекает клаим {@code realm_access} из JWT-токена как {@link Map}.</li>
     *   <li>Если клаим отсутствует или пуст — возвращает пустой список (анонимный пользователь).</li>
     *   <li>Извлекает список ролей из клаима {@code roles}.</li>
     *   <li>Если список ролей отсутствует — возвращает пустой список с предупреждением в логе.</li>
     *   <li>Каждую роль преобразует в {@code SimpleGrantedAuthority} с префиксом {@code ROLE_}.</li>
     *   <li>Возвращает результат в виде {@link java.util.Set}.</li>
     * </ol>
     *
     * @param jwt JWT-токен, полученный от Keycloak
     * @return коллекция полномочий (авторизационных ролей) для текущего пользователя
     */
    @Override
    @SuppressWarnings("java:S2638")
    public  Collection<GrantedAuthority> convert(Jwt jwt) {

        Map<String, Object> realmAccess = jwt.getClaimAsMap(ROLES_CLAIM_NAME);

        if (realmAccess == null || realmAccess.isEmpty()) {
            return List.of();
        }

        if (!realmAccess.containsKey(ROLES_KEY)) {
            log.warn("claim {} no contains key {}", ROLES_CLAIM_NAME, ROLES_KEY);
            return List.of();
        }

        Collection<String> roles = (Collection<String>) realmAccess.get(ROLES_KEY);
        if (roles == null) {
            return List.of();
        }

        return roles.stream()
                .map(role -> "ROLE_" + role)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet());
    }
}
