package com.autodev.platformservice.exception;

/**
 * Эта сущность нужна только когда пользователь прислал кривой JSON в тело запроса (например, при регистрации или редактировании профиля), и Spring (@Valid) выбросил MethodArgumentNotValidException.
 * * Она описывает конкретное поле, которое не прошло проверку валидации.
 *
 * @param field  - поле, которое не прошло валидацию
 * @param message - человекочитаемое содержание ошибки
 * */
public record FieldViolation(
        String field,
        String message
) {
}
