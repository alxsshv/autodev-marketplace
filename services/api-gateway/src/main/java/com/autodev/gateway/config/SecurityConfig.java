package com.autodev.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Mono;

/**
 * Конфигурация безопасности для API Gateway.
 * <p>
 * Этот класс настраивает Spring Security для работы в режиме OAuth2 Resource Server,
 * обеспечивая валидацию JWT токенов через Keycloak (Direct Integration).
 */
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String issuerUri;

    /**
     * Настраивает правила безопасности для API Gateway.
     * <p>
     * <b>Открытые эндпоинты (без аутентификации):</b>
     * <ul>
     *   <li>/actuator/** - эндпоинты мониторинга и управления (Health, Metrics)</li>
     *   <li>/swagger-ui/** - Swagger UI</li>
     *   <li>/v3/api-docs/** - OpenAPI спецификация</li>
     *   <li>/swagger-resources/** - ресурсы Swagger</li>
     * </ul>
     * <p>
     * <b>Защищённые эндпоинты:</b>
     * <p>
     * Все остальные эндпоинты требуют валидного JWT токена. Gateway проверяет:
     * <ul>
     *   <li>Валидность подписи токена через Keycloak</li>
     *   <li>Срок действия токена (exp, nbf)</li>
     *   <li>Correct issuer (iss)</li>
     *   <li>Correct audience (aud)</li>
     * </ul>
     * <p>
     * <b>Обработка токенов:</b>
     * <p>
     * После валидации JWT токен преобразуется в Authentication с помощью
     * {@link com.autodev.gateway.security.KeycloakReactiveJwtAuthenticationConverter}, который извлекает claims (sub, email, name)
     * для логирования и аудита.
     *
     * @param http                 конфигурация HTTP безопасности для реактивных приложений
     * @param jwtAuthenticationConverter конвертер JWT в Authentication token
     * @return настроенный SecurityWebFilterChain для валидации JWT через Keycloak
     */
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(
            ServerHttpSecurity http, Converter<Jwt, Mono<AbstractAuthenticationToken>> jwtAuthenticationConverter) {
        http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchanges ->
                        exchanges
                                .pathMatchers( "/actuator/**",
                                        "/swagger-ui/**",
                                        "/v3/api-docs/**",
                                        "/swagger-resources/**")
                                .permitAll()
                                .anyExchange().authenticated())
                .oauth2ResourceServer(oauth2 ->
                        oauth2.jwt(jwtSpec ->
                                jwtSpec.jwtAuthenticationConverter(jwtAuthenticationConverter)));
                return  http.build();
    }



}
