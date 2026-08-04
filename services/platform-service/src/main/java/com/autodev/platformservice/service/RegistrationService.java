package com.autodev.platformservice.service;

import com.autodev.platformservice.client.keycloak.KeycloakAdminClient;
import com.autodev.platformservice.dto.RegisterRequestDto;
import com.autodev.platformservice.dto.event.UserRegisteredEventPayload;
import com.autodev.platformservice.entity.DomainEvent;
import com.autodev.platformservice.entity.UserEvents;
import com.autodev.platformservice.exception.KeycloakInfrastructureException;
import com.autodev.platformservice.exception.RegistrationOperationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.UUID;

/**
 * Сервис для выполнения бизнес-логики регистрации новых пользователей на платформе.
 * <p>
 * Координирует процесс регистрации, который включает три основных шага:
 * <ol>
 *   <li>Создание учётной записи в Keycloak через {@link KeycloakAdminClient}.</li>
 *   <li>Создание локального профиля пользователя в БД через {@link UserProfileService}.</li>
 *   <li>Публикация доменного события {@code USER_REGISTERED} в outbox через {@link OutboxService}.</li>
 * </ol>
 * <p>
 * <b>Saga Outbox:</b> шаги 2 и 3 выполняются в рамках единой database-транзакции,
 * гарантирующей атомарность создания профиля и записи outbox-события.
 * <p>
 * <b>Compensating Transaction:</b> если шаги 2 или 3 завершаются неудачно,
 * выполняется компенсирующая транзакция — удаление созданного пользователя
 * из Keycloak через {@link #compensateKeycloakUserCreation(UUID)}, чтобы
 * избежать создания «полупрофилей» в системе.
 * <p>
 * <b>Пример потока регистрации:</b>
 * <pre>{@code
 * // Успешный путь:
 * registerUser(dto) -> Keycloak: CREATE -> DB: CREATE PROFILE -> Outbox: SAVE EVENT -> SUCCESS
 *
 * // Путь с ошибкой (Компенсационная транзакция):
 * registerUser(dto) -> Keycloak: CREATE -> DB: FAIL -> Keycloak: DELETE (compensate) -> THROW
 * }</pre>
 *
 * @see KeycloakAdminClient
 * @see UserProfileService
 * @see OutboxService
 * @see RegistrationOperationException
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RegistrationService {

    /**
     * Клиент административного API Keycloak для создания и удаления пользователей.
     * Инъектируется через Spring.
     */
    private final KeycloakAdminClient keycloakAdminClient;

    /**
     * Шаблон транзакций Spring для выполнения шагов в рамках единой database-транзакции.
     * <p>
     * Обеспечивает атомарность создания профиля пользователя и записи outbox-события.
     */
    private final TransactionTemplate transactionTemplate;

    /**
     * Сервис для выполнения операций с профилем пользователя.
     * Инъектируется через Spring.
     */
    private final UserProfileService userProfileService;

    /**
     * Сервис для записи доменных событий в outbox.
     * Инъектируется через Spring.
     */
    private final OutboxService outboxService;

    /**
     * Выполняет полную процедуру регистрации нового пользователя.
     * <p>
     * <b>Алгоритм:</b>
     * <ol>
     *   <li>Создаёт пользователя в Keycloak через {@link KeycloakAdminClient#createKeycloakUser(RegisterRequestDto)}.</li>
     *   <li>В рамках транзакции:
     *       <ul>
     *         <li>Создаёт локальный профиль через {@link UserProfileService#createProfileIfNotExists(String, String)}.</li>
     *         <li>Сохраняет событие {@code USER_REGISTERED} в outbox через {@link OutboxService#publishEvent(DomainEvent, UUID, Object)}.</li>
     *       </ul>
     *   </li>
     *   <li>В случае ошибки — вызывает компенсирующую транзакцию {@link #compensateKeycloakUserCreation(UUID)}
     *       и выбрасывает {@link RegistrationOperationException}.</li>
     * </ol>
     * <p>
     * <b>Обработка ошибок:</b>
     * <ul>
     *   <li>Если создание профиля или публикация outbox-события завершаются неудачей,
     *       пользователь удаляется из Keycloak (compensating transaction).</li>
     *   <li>Если компенсация тоже не удалась — ошибка логируется, требуется ручное вмешательство.</li>
     * </ul>
     *
     * @param dto данные для регистрации пользователя
     * @throws RegistrationOperationException если регистрация не удалась (профиль не создан, outbox недоступен, компенсация не удалась)
     */
    public void registerUser(RegisterRequestDto dto) {
        String email = dto.email();
        log.info("Starting registration for email: {}", email);

        UUID keycloakUserId = keycloakAdminClient.createKeycloakUser(dto);

        UserRegisteredEventPayload eventPayload = new UserRegisteredEventPayload(
                keycloakUserId.toString(),
                email,
                dto.firstName(),
                dto.lastName()
        );

        try {
            log.info("User created in Keycloak for email {} with keycloakUserId = {}", email, keycloakUserId);

            transactionTemplate.executeWithoutResult(status -> {
                userProfileService.createProfileIfNotExists(keycloakUserId.toString(), email);
                outboxService.publishEvent(UserEvents.USER_REGISTERED, keycloakUserId, eventPayload);
            });
        } catch (Exception ex) {
            log.error("Registration failed for email {}, initiating compensating transaction", email, ex);
            compensateKeycloakUserCreation(keycloakUserId);
            throw new RegistrationOperationException("Ошибка при создании профиля для пользователя", ex);
        }
    }

    /**
     * Выполняет компенсирующую транзакцию — удаляет пользователя из Keycloak.
     * <p>
     * Вызывается при неудаче на шагах 2 или 3 процесса регистрации.
     * Гарантирует, что пользователь не останется «застрявшим» в Keycloak
     * без соответствующего профиля в локальной БД.
     * <p>
     * <b>Обработка ошибок компенсации:</b>
     * <ul>
     *   <li>Если удаление успешно — логируется информация.</li>
     *   <li>Если удаление не удалось — логируется критическая ошибка с UUID пользователя
     *       и сообщением о необходимости ручного вмешательства.</li>
     * </ul>
     *
     * @param keycloakUserId UUID пользователя Keycloak для удаления
     */
    private void compensateKeycloakUserCreation(UUID keycloakUserId) {
        try {
            keycloakAdminClient.deleteKeycloakUser(keycloakUserId.toString());
            log.info("Compensating transaction: successfully deleted Keycloak user {}", keycloakUserId);
        } catch (KeycloakInfrastructureException ex) {
            log.error("COMPENSATION FAILED: Could not delete Keycloak user {} error: {}. Manual intervention required!", keycloakUserId, ex.getMessage());
        }
    }

}
