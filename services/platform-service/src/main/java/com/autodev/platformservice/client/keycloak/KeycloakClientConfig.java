package com.autodev.platformservice.client.keycloak;

import com.autodev.platformservice.config.KeycloakProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestClient;

/**
 * Конфигурация REST-клиента для взаимодействия с Keycloak Admin API.
 * <p>
 * Создает и настраивает bean {@link RestClient} для выполнения запросов
 * к административному API Keycloak. Автоматически добавляет заголовки
 * Content-Type, настраивает JSON-конвертеры и подключает интерсептор
 * аутентификации.
 */
@Configuration
@RequiredArgsConstructor
public class KeycloakClientConfig {

    private final KeycloakProperties keycloakProps;

    private final KeycloakAuthInterceptor keycloakAuthInterceptor;


    /**
     * Создает сконфигурированный RestClient для взаимодействия с Keycloak.
     * <p>
     * Базовый URL формируется из настроек Keycloak с добавлением пути к
     * административному API и realm'у. Включает автоматическую аутентификацию
     * через {@link KeycloakAuthInterceptor} и поддержку JSON через Jackson.
     *
     * @param objectMapper объект ObjectMapper для обработки JSON
     * @return настроенный RestClient для работы с Keycloak Admin API
     */
    @Bean
    public RestClient keycloakRestClient(ObjectMapper objectMapper) {
        String baseUrl = keycloakProps.serverUrl() + "/admin/realms/" + keycloakProps.realm();

        return RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .requestInterceptor(keycloakAuthInterceptor)
                .messageConverters(httpMessageConverters ->
                        httpMessageConverters.add(new MappingJackson2HttpMessageConverter(objectMapper)))
                .build();
    }
}
