package com.autodev.platformservice.config;

import com.autodev.platformservice.security.KeycloakJwtAuthenticationConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Конфигурация безопасности Spring Security для REST-клиента платформы.
 * <p>
 * Настраивает защиту приложения на основе JWT-токенов Keycloak (OAuth2 Resource Server):
 * <ul>
 *   <li><b>CSRF отключён</b> — приложение работает как stateless REST-сервис без сессий.</li>
 *   <li><b>Stateless-сессии</b> — состояние не хранится на сервере, каждый запрос содержит JWT.</li>
 *   <li><b>Публичные эндпоинты</b> — {@code /actuator/health} и {@code /registration}
 *       доступны без аутентификации (health-check и регистрация нового пользователя).</li>
 *   <li><b>Все остальные запросы</b> требуют аутентификации через валидный JWT-токен Keycloak.</li>
 *   <li><b>Преобразование JWT</b> — полномочия извлекаются из claim {@code realm_access.roles}
 *       через {@link KeycloakJwtAuthenticationConverter} с префиксом {@code ROLE_}.</li>
 * </ul>
 * <p>
 * <b>Метод-level security:</b> включён через {@link EnableMethodSecurity},
 * что позволяет аннотировать методы сервисов и контроллеров аннотациями
 * {@code @PreAuthorize}, {@code @PostAuthorize}, {@code @Secured}.
 *
 * @see KeycloakJwtAuthenticationConverter
 * @see org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    /**
     * Конфигурирует цепочку фильтров безопасности.
     * <p>
     * <b>Настройки:</b>
     * <ul>
     *   <li>Отключена защита от CSRF (межсайтовая подделка запроса).</li>
     *   <li>Отключено хранение сессий (stateless).</li>
     *   <li>Публичные URI: {@code /actuator/health}, {@code /registration}.</li>
     *   <li>Все остальные URI требуют аутентификации.</li>
     *   <li>OAuth2 Resource Server с JWT-конвертером для извлечения ролей.</li>
     * </ul>
     *
     * @param http объект для конфигурации HTTP-безопасности
     * @return настроенная цепочка фильтров безопасности
     * @throws Exception если ошибка конфигурации безопасности
     */
    @SuppressWarnings("java:S4502")
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)

                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(requests -> requests
                        .requestMatchers("/actuator/health", "/registration").permitAll()
                        .anyRequest().authenticated()
                )

                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())
                        )
                );

        return http.build();
    }

    /**
     * Создаёт бин для преобразования JWT-токена в объект аутентификации Spring Security.
     * <p>
     * Настраивает {@link JwtAuthenticationConverter} с кастомным
     * {@link KeycloakJwtAuthenticationConverter}, который извлекает роли
     * из клаима {@code realm_access.roles} Keycloak и преобразует их
     * в {@code SimpleGrantedAuthority} с префиксом {@code ROLE_}.
     *
     * @bean jwtAuthenticationConverter преобразователь JWT-токена в список полномочий
     * @return настроенный JwtAuthenticationConverter
     */
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
         JwtAuthenticationConverter jwtAuthenticationConverter =  new JwtAuthenticationConverter();
         jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(new KeycloakJwtAuthenticationConverter());
         return jwtAuthenticationConverter;
    }

}
