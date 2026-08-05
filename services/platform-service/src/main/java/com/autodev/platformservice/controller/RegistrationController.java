package com.autodev.platformservice.controller;

import com.autodev.platformservice.dto.RegisterRequestDto;
import com.autodev.platformservice.service.RegistrationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * REST-контроллер для регистрации новых пользователей платформы.
 * <p>
 * Предоставляет один публичный endpoint — POST {@code /api/v1/platform/registration} —
 * для создания учётной записи. Процесс регистрации включает:
 * <ol>
 *   <li>Валидацию входных данных (JSON-схема + {@code @Valid}).</li>
 *   <li>Создание пользователя в Keycloak (административное API).</li>
 *   <li>Создание внутреннего профиля пользователя в БД (в той же транзакции).</li>
 *   <li>Публикацию события {@code USER_REGISTERED} в outbox для асинхронной доставки.</li>
 * </ol>
 * <p>
 * В случае ошибки на этапе создания профиля (например, потеря соединения с БД)
 * выполняется компенсирующая транзакция — удаление пользователя из Keycloak.
 * <p>
 * <b>Безопасность:</b> endpoint публичный (не требует аутентификации),
 * что позволяет регистрироваться новым пользователям без предварительного входа.
 *
 * @see RegistrationService
 * @see RegisterRequestDto
 */
@RestController
@RequestMapping("/api/v1/platform")
@Slf4j
@RequiredArgsConstructor
public class RegistrationController {

    /**
     * Сервис для выполнения бизнес-логики регистрации пользователя.
     * Инъектируется через Spring.
     */
    private final RegistrationService registrationService;

    /**
     * Обрабатывает POST-запрос на регистрацию нового пользователя.
     * <p>
     * Принимает валидированный DTO с данными пользователя (email, пароль, имя, фамилия),
     * вызывает {@link RegistrationService#registerUser(RegisterRequestDto)} для
     * создания учётной записи в Keycloak, профиля в БД и публикации outbox-события.
     * <p>
     * Возвращает статус {@code 201 Created} без тела ответа. В случае ошибок
     * глобальный обработчик {@link com.autodev.platformservice.exception.GlobalExceptionHandler}
     * вернёт соответствующий HTTP-статус (400, 409, 500).
     *
     * @param dto данные для регистрации, валидированные аннотациями {@code @Valid}
     * @throws org.springframework.web.bind.MethodArgumentNotValidException при невалидных входных данных (400)
     * @see HttpStatus#CREATED
     */
    @PostMapping("/registration")
    @ResponseStatus(HttpStatus.CREATED)
    public void register(@RequestBody @Valid RegisterRequestDto dto) {
        log.debug("Received registration request for email = {}", dto.email());
        registrationService.registerUser(dto);
    }

}
