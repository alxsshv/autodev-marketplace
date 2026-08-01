package com.autodev.platformservice.exception;

/**
 * Исключение выбрасывается, когда профиль пользователя не найден.
 * <p>
 * Возникает в сервисном слое, когда поиск по {@code keycloakUserId}
 * не возвращает существующий профиль, и автоматическое создание
 * также невозможно (например, из-за нарушения целостности данных).
 * <p>
 * Перехватывается глобальным обработчиком {@link com.autodev.platformservice.exception.GlobalExceptionHandler}
 * и преобразуется в HTTP-ответ со статусом {@code 404 Not Found}.
 * <p>
 * <b>Примеры использования:</b>
 * <ul>
 *   <li>Возвращает сообщение об ошибке «Профиль пользователя не найден».</li>
 *   <li>Поддерживает форматирование сообщения через {@code String.format}.</li>
 * </ul>
 *
 * @see com.autodev.platformservice.service.UserProfileService
 * @see com.autodev.platformservice.exception.GlobalExceptionHandler#handleUserProfileNotFoundException(com.autodev.platformservice.exception.UserProfileNotFoundException, jakarta.servlet.http.HttpServletRequest)
 */
public class UserProfileNotFoundException extends RuntimeException {

    /**
     * Создаёт исключение с сообщением об ошибке.
     *
     * @param message текстовое описание ошибки
     */
    public UserProfileNotFoundException(String message) {
        super(message);
    }

    /**
     * Создаёт исключение с отформатированным сообщением об ошибке.
     * <p>
     * Использует {@code String.format(message, args)} для подстановки
     * аргументов в строку сообщения (например, подстановка ID пользователя).
     *
     * @param message шаблон сообщения, поддерживающий {@code String.format}
     * @param args аргументы для форматирования сообщения
     */
    public UserProfileNotFoundException(String message, Object... args) {
        super(String.format(message, args));
    }
}
