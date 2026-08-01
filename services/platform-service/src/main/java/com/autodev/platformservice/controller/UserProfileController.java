package com.autodev.platformservice.controller;

import com.autodev.platformservice.dto.UpdateProfileRequestDto;
import com.autodev.platformservice.dto.UserProfileResponseDto;
import com.autodev.platformservice.security.SecurityUtils;
import com.autodev.platformservice.service.UserProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * REST-контроллер для управления профилем текущего аутентифицированного пользователя.
 * <p>
 * Предоставляет endpoint'ы для получения и обновления профиля пользователя:
 * <ul>
 *   <li>GET {@code /api/v1/platform/profile} — получить текущий профиль пользователя.</li>
 *   <li>PUT {@code /api/v1/platform/profile} — обновить данные профиля.</li>
 * </ul>
 * <p>
> <b>Безопасность:</b> все методы защищены — требуется валидный JWT-токен Keycloak.
 * Идентификатор пользователя извлекается из JWT через {@link SecurityUtils}.
 * <p>
 * <b>Обновляемые поля профиля:</b>
 * <ul>
 *   <li>Название магазина ({@code storeName}).</li>
 *   <li>Описание магазина ({@code storeDescription})..</li>
 *   <li>Номер телефона ({@code phone})..</li>
 * </ul>
 * <p>
 * <b>Необновляемые через этот endpoint:</b> email, статус верификации,
 * лояльность баланс, аудиторские поля — эти данные изменяются через
 * другие сервисы (верификация, биллинг).
 *
 * @see UserProfileService
 * @see UpdateProfileRequestDto
 * @see UserProfileResponseDto
 */
@RestController
@RequestMapping("/api/v1/platform/profile")
@RequiredArgsConstructor
@Slf4j
public class UserProfileController {

    /**
     * Сервис для выполнения бизнес-логики управления профилем пользователя.
     * Инъектируется через Spring.
     */
    private final UserProfileService userProfileService;

    /**
     * Возвращает профиль текущего аутентифицированного пользователя.
     * <p>
     * Если профиль не найден, автоматически создаётся новый профиль
     * с базовыми значениями (email из JWT, статус NOT_VERIFIED).
     * <p>
     * Возвращает статус {@code 200 OK} с телом ответа {@code UserProfileResponseDto}.
     *
     * @return профиль текущего пользователя
     * @throws com.autodev.platformservice.exception.KeycloakInfrastructureException если ошибка интеграции с Keycloak (500)
     */
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public UserProfileResponseDto getCurrentProfile() {
        return userProfileService.getCurrentUserProfile();
    }

    /**
     * Обновляет данные профиля текущего пользователя.
     * <p>
     * Принимает валидированный DTO с новыми значениями полей профиля
     * и обновляет их в базе данных. Ключевые поля (email, ключевые данные —
     * не меняются через этот endpoint.
     * <p>
     * Возвращает статус {@code 200 OK} с обновлённым профилем в теле ответа.
     *
     * @param dto обновляемые данные профиля, валидированные аннотациями {@code @Valid}
     * @return обновлённый профиль пользователя
     * @throws com.autodev.platformservice.exception.UserProfileNotFoundException если профиль не найден (404)
     * @throws org.springframework.web.bind.MethodArgumentNotValidException при невалидных входных данных (400)
     */
    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    public UserProfileResponseDto updateProfile(@Valid @RequestBody UpdateProfileRequestDto dto) {
        String keycloakUserId = SecurityUtils.getCurrentUserId();
        return userProfileService.updateUserProfile(dto, keycloakUserId);
    }
}
