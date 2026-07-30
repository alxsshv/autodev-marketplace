package com.autodev.platformservice.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.messaging.handler.annotation.support.MethodArgumentNotValidException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;

/**
 * Глобальный обработчик исключений для REST-контроллеров.
 * Перехватывает исключения, возникающие в слое контроллеров, и возвращает
 * единообразный ответ в формате {@link ErrorResponse}.
 */
@RestControllerAdvice
@Slf4j
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final Clock clock;

    /**
     * Обрабатывает ошибки валидации аргументов методов (@Valid).
     * Возвращает 400 Bad Request со списком нарушенных полей.
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
     *
     * @param ex исключение валидации аргументов
     * @return список {@link FieldViolation} с именем поля и сообщением об ошибке
     */
    @SuppressWarnings("java:S2259")
    private List<FieldViolation> extractViolation(MethodArgumentNotValidException ex) {
        if (ex.getBindingResult() != null) {
            return Objects.requireNonNull(ex.getBindingResult()).getFieldErrors().stream()
                    .map(fieldError -> new FieldViolation(fieldError.getField(), fieldError.getDefaultMessage()))
                    .toList();
        }
        return List.of();
    }

    /**
     * Обрабатывает ошибки парсинга тела HTTP-запроса (некорректный JSON).
     * Возвращает 400 Bad Request.
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
     * Возвращает 403 Forbidden.
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

    @ExceptionHandler(KeycloakInfrastructureException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleKeycloakInfrastructureException(KeycloakInfrastructureException ex, HttpServletRequest request) {

        log.error("Ошибка при взаимодействии с сервисом авторизации при выполнении запроса по пути {} : {}",
                request.getRequestURI(), ex.getMessage());

        return new ErrorResponse(
                OffsetDateTime.now(clock),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                ex.getMessage(),
                request.getRequestURI(),
                null
        );
    }

    @ExceptionHandler(RegistrationOperationException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleKeycloakInfrastructureException(RegistrationOperationException ex, HttpServletRequest request) {

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


    @ExceptionHandler(UserAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleUserAlreadyExistsException(KeycloakInfrastructureException ex, HttpServletRequest request) {

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
     * Обрабатывает все непредвиденные исключения, не перехваченные другими обработчиками.
     * Возвращает 500 Internal Server Error.
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
