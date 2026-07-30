package com.autodev.platformservice.exception;

/**
 * Исключение, возникающее при ошибках инфраструктуры Keycloak.
 * <p>
 * Предназначено для обозначения проблем, связанных с интеграцией с Keycloak,
 * таких, как недоступность сервиса, ошибки сети или сбои в инфраструктуре.
 * Является unchecked исключением (расширяет {@link RuntimeException}).
 */
public class KeycloakInfrastructureException extends RuntimeException {
    /**
     * Создает новое исключение с заданным сообщением.
     *
     * @param message сообщение об ошибке
     */
    public KeycloakInfrastructureException(String message) {
        super(message);
    }

    public KeycloakInfrastructureException(String message, Object... args) {
        super(String.format(message, args));
    }
}
