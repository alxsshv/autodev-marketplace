package com.autodev.platformservice.exception;

/**
 * Исключение выбрасывается при ошибке выполнения операций регистрации пользователя.
 * <p>
 * Возникает в {@link com.autodev.platformservice.service.RegistrationService}
 * когда создание профиля пользователя в БД или публикация outbox-события
 * завершается неудачно. В этом случае инициируется компенсирующая
 * транзакция — удаление пользователя из Keycloak.
 * <p>
 * Перехватывается глобальным обработчиком {@link com.autodev.platformservice.exception.GlobalExceptionHandler}
 * и преобразуется в HTTP-ответ со статусом {@code 500 Internal Server Error}.
 * <p>
 * <b>Примеры использования:</b>
 * <ul>
 *   <li>«Ошибка при создании профиля для пользователя» — когда база данных недоступна.</li>
 *   <li>«Ошибка при создании профиля для пользователя: ...» — с указанием причины в cause.</li>
 * </ul>
 *
 * @see com.autodev.platformservice.service.RegistrationService#registerUser(com.autodev.platformservice.dto.RegisterRequestDto)
 * @see com.autodev.platformservice.exception.GlobalExceptionHandler#handleRegistrationOperationException(com.autodev.platformservice.exception.RegistrationOperationException, jakarta.servlet.http.HttpServletRequest)
 */
public class RegistrationOperationException extends RuntimeException {

    /**
     * Создаёт исключение с сообщением об ошибке.
     *
     * @param message текстовое описание ошибки
     */
    public RegistrationOperationException(String message) {
        super(message);
    }

    /**
     * Создаёт исключение с отформатированным сообщением об ошибке.
     * <p>
     * Использует {@code String.format(message, args)} для подстановки
     * аргументов в строку сообщения.
     *
     * @param message шаблон сообщения, поддерживающий {@code String.format}
     * @param args аргументы для форматирования сообщения
     */
    public RegistrationOperationException(String message, Object... args) {
        super(String.format(message, args));
    }

    /**
     * Создаёт исключение с сообщением об ошибке и причиной (cause).
     * <p>
     * Используется при перехвате системных исключений (например,
     * {@link org.springframework.dao.DataAccessResourceFailureException})
     * для сохранения стека вызовов.
     *
     * @param message текстовое описание ошибки
     * @param cause причина исключения, вызвавшего данное исключение
     */
    public RegistrationOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
