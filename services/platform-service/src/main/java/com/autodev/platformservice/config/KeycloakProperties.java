package com.autodev.platformservice.config;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Конфигурационные свойства для подключения к административному API Keycloak.
 * <p>
 * Связывается с свойствами, имеющими префикс {@code keycloak.admin}, из файла
 * application.yml / application.properties. Содержит параметры для аутентификации
 * сервисной учётной записи Keycloak (client credentials flow).
 * <p>
 * <b>Пример конфигурации в application.yml:</b>
 * <pre>{@code
 * keycloak:
 *   admin:
 *     server-url: https://keycloak.example.com
 *     realm: platform-realm
 *     client-id: platform-service
 *     client-secret: *****
 * }</pre>
 * <p>
 * <b>Безопасность:</b> поле {@code clientSecret} отмечено аннотацией
 * {@link IgnoreJson @JsonIgnore}, чтобы исключить его из логов и сериализации
 * (метод {@link #toString()} не включает секретное значение).
 *
 * @see org.springframework.boot.context.properties.ConfigurationProperties
 * @see KeycloakTokenProvider
 */
@ConfigurationProperties(prefix = "keycloak.admin")
public record KeycloakProperties(

        /**
         * Полная ссылка на сервер Keycloak (scheme + host + port).
         * <p>
         * Пример: {@code https://keycloak.example.com/auth}
         */
        String serverUrl,

        /**
         * Имя realm (домена) Keycloak.
         * <p>
         * Пример: {@code platform-realm}
         */
        String realm,

        /**
         * Имя клиентского приложения (client ID) в Keycloak.
         * <p>
         * Используется для service account аутентификации.
         * Пример: {@code platform-service}
         */
        String clientId,

        /**
         * Секретный ключ клиентского приложения (client secret).
         * <p>
         * <b>Не выводится в логах и не участвует в сериализации JSON.</b>
         */
        @JsonIgnore
        String clientSecret

) {
    /**
     * Возвращает строковое представление свойств без секретного ключа.
     * <p>
     * Переопределяет стандартный {@code toString()} для безопасности:
     * исключает {@code clientSecret} из выводимых данных, чтобы избежать
     * утечки секрета в логах.
     *
     * @return строковое представление без clientSecret
     */
    @Override
    public String toString() {
        return "KeycloakAdminProperties{" +
                "serverUrl='" + serverUrl + '\'' +
                ", realm='" + realm + '\'' +
                ", clientId='" + clientId + '\'' +
                '}';
    }
}
