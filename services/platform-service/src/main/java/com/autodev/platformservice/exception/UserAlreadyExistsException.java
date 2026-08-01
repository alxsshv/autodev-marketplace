package com.autodev.platformservice.exception;

/**
 * Исключение выбрасывается при попытке создать пользователя, который уже существует.
 * <p>
 * Возникает в {@link com.autodev.platformservice.client.keycloak.KeycloakAdminClient}
 * когда Keycloak возвращает статус {@code 409 Conflict} — пользователь с таким
 * email-адресом уже зарегистрирован в системе.
 * <p>
 * Перехватывается глобальным обработчиком {@link com.autodev.platformservice.exception.GlobalExceptionHandler}
 * и преобразуется в HTTP-ответ со статусом {@code 409 Conflict}.
 * <p>
 * <b>Пример использования:</b>
 * <pre>{@code
 * throw new UserAlreadyExistsException("Пользователь с email %s уже существует", dto.email());
 * }</pre>
 *
 * @see com.autodev.platformservice.client.keycloak.KeycloakAdminClient#createKeycloakUser(com.autodev.platformservice.dto.RegisterRequestDto)
 * @see com.autodev.platformservice.exception.GlobalExceptionHandler#handleUserAlreadyExistsException(com.autodev.platformservice.exception.UserAlreadyExistsException, jakarta.servlet.http.HttpServletRequest)
 */
public class UserAlreadyExistsException extends RuntimeException {

    /**
     * Создаёт исключение с сообщением об ошибке.
     *
     * @param message текстовое описание ошибки
     */
    public UserAlreadyExistsException(String message) {
        super(message);
    }

    /**
     * Создаёт исключение с отформатированным сообщением об ошибке.
     * <p>
     * Использует {@code String.format(message, args)} для подстановки
     * аргументов в строку сообщения (например, подстановка email).
     *
     * @param message шаблон сообщения, поддерживающий {@code String.format}
     * @param args аргументы для форматирования сообщения
     */
    public UserAlreadyExistsException(String message, Object... args) {
        super(String.format(message, args));
    }
}
