package com.autodev.platformservice.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;

/**
 * Глобальный обработчик исключений для REST-контроллеров.
 * <p>
 * Аннотирован {@link RestControllerAdvice @RestControllerAdvice}, что означает
 * автоматическое перехватывание исключений, возникающих в контроллерах этого приложения.
 * Переводит исключения в единообразный формат {@link ErrorResponse} с HTTP-статусом.
 * <p>
 * <b>Обрабатываемые исключения и HTTP-статусы:</b>
 * <table summary="Соответствие исключений и HTTP-статусов">
 *   <tr>
 *     <th>Исключение</th>
 *     <th>HTTP-статус</th>
 *     <th>Уровень лога</th>
 *   </tr>
 *   <tr>
 *     <td>{@link MethodArgumentNotValidException}</td>
 *     <td>400 Bad Request</td>
 *     <td>WARN</td>
 *   </tr>
 *   <tr>
 *     <td>{@link HttpMessageNotReadableException}</td>
 *     <td>400 Bad Request</td>
 *     <td>WARN</td>
 *   </tr>
 *   <tr>
 *     <td>{@link AccessDeniedException}</td>
 *     <td>403 Forbidden</td>
 *     <td>WARN</td>
 *   </tr>
 *   <tr>
 *     <td>{@link KeycloakInfrastructureException}</td>
 *     <td>500 Internal Server Error</td>
 *     <td>ERROR</td>
 *   </tr>
 *   <tr>
 *     <td>{@link RegistrationOperationException}</td>
 *     <td>500 Internal Server Error</td>
 *     <td>ERROR</td>
 *   </tr>
 *   <tr>
 *     <td>{@link UserAlreadyExistsException}</td>
 *     <td>409 Conflict</td>
 *     <td>ERROR</td>
 *   </tr>
 *   <tr>
 *     <td>{@link UserProfileNotFoundException}</td>
 *     <td>404 Not Found</td>
 *     <td>WARN</td>
 *   </tr>
 *   <tr>
 *     <td>{@link Exception} (все остальные)</td>
 *     <td>500 Internal Server Error</td>
 *     <td>ERROR</td>
 *   </tr>
 * </table>
 *
 * @see ErrorResponse
 * @see com.autodev.platformservice.controller.RegistrationController
 * @see com.autodev.platformservice.controller.UserProfileController
 */
@RestControllerAdvice
@Slf4j
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    /**
     * Часы для получения текущего времени.
     * <p>
     * Инъектируется через Spring для обеспечения тестируемости
     * (возможность подмены на фиктивное время).
     */
    private final Clock clock;

    /**
     * Обрабатывает ошибки валидации аргументов методов, аннотированных {@code @Valid}.
     * <p>
     * Перехватывает исключения, возникающие при проверке аннотированных полей
     * в DTO (например, {@code @NotBlank}, {@code @Email}, {@code @Size}).
     * Возвращает {@code 400 Bad Request} с подробным списком нарушенных полей
     * в формате {@link FieldViolation}.
     * <p>
     * <b>Пример ответа:</b>
     * <pre>{@code
     * {
     *   "timestamp": "2024-01-15T10:30:00Z",
     *   "status": 400,
     *   "message": "Ошибка валидации входных данных",
     *   "path": "/api/v1/platform/registration",
     *   "violations": [
     *     { "field": "email", "message": "Неверный формат адреса электронной почты" },
     *     { "field": "password", "message": "Пароль должен быть не менее 6 символов" }
     *   ]
     * }
     * }</pre>
     *
     * @param ex исключение валидации аргументов
     * @param request HTTP-запрос, вызвавший исключение
     * @return ответ с деталями нарушения полей
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleMethodArgumentNotValidException(MethodArgumentNotValidException ex,
                                                               HttpServletRequest request) {

        log.warn("Ошибка валидации запроса по пути {}: {}", request.getRequestURI(), ex.getMessage());

        List<FieldViolation> violations = extractViolation(ex);

        return new ErrorResponse(
                OffsetDateTime.now(clock),
                HttpStatus.BAD_REQUEST.value(),
                "Ошибка валидации входных данных",
                request.getRequestURI(),
                violations
        );
    }

    /**
     * Извлекает список нарушений полей из исключения валидации.
     * <p>
     * Преобразует {@link org.springframework.validation.FieldError} из
     * {@code BindingResult} в список {@link FieldViolation}.
     * Если {@code BindingResult} нулевой, возвращает пустой список.
     *
     * @param ex исключение валидации аргументов
     * @return список нарушений полей, или пустой список, если нет данных
     */
    @SuppressWarnings("java:S2259")
    private List<FieldViolation> extractViolation(MethodArgumentNotValidException ex) {
            return Objects.requireNonNull(ex.getBindingResult()).getFieldErrors().stream()
                    .map(fieldError -> new FieldViolation(fieldError.getField(), fieldError.getDefaultMessage()))
                    .toList();
    }

    /**
     * Обрабатывает ошибки парсинга тела HTTP-запроса.
     * <p>
     * Перехватывает исключения, возникающие при некорректном JSON
     * (например, пропущенная кавычка, недопустимый тип значения,
     * синтаксическая ошибка JSON). Возвращает {@code 400 Bad Request}.
     * <p>
     * <b>Пример причины:</b> клиент отправил {@code {"email": invalid}} вместо {@code {"email": "test@example.com"}}.
     *
     * @param ex исключание нечитаемого HTTP-сообщения
     * @param request HTTP-запрос, вызвавший исключение
     * @return ответ с описанием ошибки парсинга
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleHttpMessageNotReadableException(HttpMessageNotReadableException ex,
                                                               HttpServletRequest request) {

        log.warn("Ошибка парсинга JSON по пути {}: {}", request.getRequestURI(), ex.getMessage());

        return new ErrorResponse(
                OffsetDateTime.now(clock),
                HttpStatus.BAD_REQUEST.value(),
                "Некорректный формат запроса. Проверьте синтаксис JSON и типы данных.",
                request.getRequestURI(),
                null
        );
    }

    /**
     * Обрабатывает исключения при попытке доступа к ресурсу без необходимых прав.
     * <p>
     * Возникает, когда аутентифицированный пользователь пытается выполнить
     * операцию, на которую у него нет полномочий (например, доступ к чужому профилю).
     * Возвращает {@code 403 Forbidden}.
     *
     * @param ex исключение отказа в доступе
     * @param request HTTP-запрос, вызвавший исключение
     * @return ответ с описанием ошибки доступа
     */
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorResponse handleAccessDeniedException(AccessDeniedException ex, HttpServletRequest request) {

        log.warn("Попытка доступа без прав по пути {}: {}", request.getRequestURI(), ex.getMessage());

        return new ErrorResponse(
                OffsetDateTime.now(clock),
                HttpStatus.FORBIDDEN.value(),
                "Доступ запрещен. У вас недостаточно прав для выполнения данной операции.",
                request.getRequestURI(),
                null
        );
    }

    /**
     * Обрабатывает исключения при взаимодействии с инфраструктурой Keycloak.
     * <p>
     * Возникает при потере соединения с Keycloak, таймауте, ошибке
     * аутентификации service account и других инфраструктурных проблемах.
     * Возвращает {@code 500 Internal Server Error}.
     *
     * @param ex инфраструктурное исключение Keycloak
     * @param request HTTP-запрос, вызвавший исключение
     * @return ответ с сообщением об ошибке интеграции
     */
    @ExceptionHandler(KeycloakInfrastructureException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public ErrorResponse handleKeycloakInfrastructureException(KeycloakInfrastructureException ex, HttpServletRequest request) {

        log.error("Ошибка при взаимодействии с сервисом авторизации при выполнении запроса по пути {} : {}",
                request.getRequestURI(), ex.getMessage());

        return new ErrorResponse(
                OffsetDateTime.now(clock),
                HttpStatus.SERVICE_UNAVAILABLE.value(),
                ex.getMessage(),
                request.getRequestURI(),
                null
        );
    }

    /**
     * Обрабатывает исключения при выполнении процедуры регистрации пользователя.
     * <p>
     * Возникает, когда создание профиля пользователя в БД или публикация
     * outbox-события завершилась неудачно (например, потеря соединения с БД).
     * Возвращает {@code 500 Internal Server Error}.
     *
     * @param ex исключение операции регистрации
     * @param request HTTP-запрос, вызвавший исключение
     * @return ответ с сообщением об ошибке регистрации
     */
    @ExceptionHandler(RegistrationOperationException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleRegistrationOperationException(RegistrationOperationException ex, HttpServletRequest request) {

        log.error("Ошибка при выполнении процедуры регистрации пользователя по пути {} : {}",
                request.getRequestURI(), ex.getMessage());

        return new ErrorResponse(
                OffsetDateTime.now(clock),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                ex.getMessage(),
                request.getRequestURI(),
                null
        );
    }

    /**
     * Обрабатывает исключения при попытке создать существующего пользователя.
     * <p>
     * Возникает, когда клиент пытается зарегистрировать пользователя
     * с email, который уже существует в Keycloak. Возвращает {@code 409 Conflict}.
     *
     * @param ex исключение существующего пользователя
     * @param request HTTP-запрос, вызвавший исключение
     * @return ответ с сообщением о конфликте
     */
    @ExceptionHandler(UserAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleUserAlreadyExistsException(UserAlreadyExistsException ex, HttpServletRequest request) {

        log.error("Ошибка создания пользователя по пути {} : {}",
                request.getRequestURI(), ex.getMessage());

        return new ErrorResponse(
                OffsetDateTime.now(clock),
                HttpStatus.CONFLICT.value(),
                ex.getMessage(),
                request.getRequestURI(),
                null
        );
    }

    /**
     * Обрабатывает исключения, когда профиль пользователя не найден.
     * <p>
     * Возникает, когда поиск профиля по {@code keycloakUserId} не возвращает
     * существующую запись, и автоматическое создание также невозможно.
     * Возвращает {@code 404 Not Found}.
     *
     * @param ex исключение отсутствия профиля
     * @param request HTTP-запрос, вызвавший исключение
     * @return ответ с сообщением о NotFound
     */
    @ExceptionHandler(UserProfileNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleUserProfileNotFoundException(UserProfileNotFoundException ex, HttpServletRequest request) {

        log.warn("Профиль пользователя не был найден во время запроса по пути {} : {}",
                request.getRequestURI(), ex.getMessage());

        return new ErrorResponse(
                OffsetDateTime.now(clock),
                HttpStatus.NOT_FOUND.value(),
                "Профиль пользователя не найден",
                request.getRequestURI(),
                null
        );

    }

    /**
     * Обрабатывает все непредвиденные исключения, не перехваченные другими обработчиками.
     * <p>
     * Служит «последней линией обороны» — перехватывает любые {@link Exception},
     * которые не были обработаны более специфичными {@code @ExceptionHandler}.
     * Возвращает пользователю обобщённое сообщение {@code 500 Internal Server Error}
     * без раскрытия внутренних деталей ошибки (безопасность).
     *
     * @param ex непредвиденное исключение
     * @param request HTTP-запрос, вызвавший исключение
     * @return ответ с сообщением о внутренней ошибке сервера
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleGenericException(Exception ex, HttpServletRequest request) {

        log.error("Непредвиденная ошибка по пути {}", request.getRequestURI(), ex);

        return new ErrorResponse(
                OffsetDateTime.now(clock),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Внутренняя ошибка сервера. Попробуйте повторить запрос позже.",
                request.getRequestURI(),
                null
        );
    }

}
