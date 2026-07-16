package com.autodev.platformservice.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.OffsetDateTime;
import java.util.List;

/**  Обёртка для любой ошибки, возвращаемой в рамках REST API
 *
 * @param timestamp - дата возникновения ошибки.
 * @param status - код статуса ошибки (целочисленное значение).
 * @param message - человекочитаемое описание ошибки.
 * @param path - URI, по которому был запрос (например, /api/v1/profiles/me).
 * @param violations - Точное описание каждой ошибки валидации (400 Bad Request), фронтенду нужно получить и общее сообщение ("Ошибка валидации"),
 *                  и конкретный список полей.
 *                   Если вынести violations в отдельный класс ответа, фронтенду придётся проверять тип ответа.
 *                   Стандарт индустрии — вложенный массив.
 *                   Аннотация @JsonInclude(NON_NULL) гарантирует,
 *                   что для ошибок типа 404 это поле просто не появится в JSON (будет null).*/
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        OffsetDateTime timestamp,
        int status,
        String message,
        String path,
        List<FieldViolation> violations
) {
}
