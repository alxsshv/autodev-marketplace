package com.autodev.platformservice.client.keycloak;

import com.autodev.platformservice.dto.RegisterRequestDto;
import com.autodev.platformservice.exception.KeycloakInfrastructureException;
import com.autodev.platformservice.exception.UserAlreadyExistsException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static com.autodev.platformservice.client.keycloak.KeycloakClientLoggingUtils.extractSafeErrorSummary;

/**
 * Клиент административного API Keycloak для управления пользователями.
 * <p>
 * Предоставляет методы для создания и удаления пользователей в Keycloak
 * через его REST API. Использует {@link ObjectMapper} для построения
 * JSON-тел запросов и {@link RestClient} для HTTP-взаимодействия
 * с сервером Keycloak.
 * <p>
 * Клиент автоматически обрабатывает типичные ошибки:
 * <ul>
 *   <li>{@code 409 Conflict} — преобразуется в {@link UserAlreadyExistsException} при создании пользователя
 *       (пользователь с таким email уже существует).</li>
 *   <li>{@code 404 Not Found} — логирование на уровне INFO при удалении (пользователь уже отсутствует, пропуск).</li>
 *   <li>{@code 4xx} и {@code 5xx} — преобразуются в {@link KeycloakInfrastructureException} с извлечением
 *       безопасного описания ошибки через {@link KeycloakClientLoggingUtils}.</li>
 * </ul>
 *
 * <h3>Жизненный цикл</h3>
 * <p>
 * Компонент является потокобезопасным для разных запросов, однако отдельные
 * вызовы методов не гарантируют атомарности на стороне Keycloak (например,
 * двойной вызов {@link #createKeycloakUser(RegisterRequestDto)} приведёт
 * к {@code 409 Conflict} для повторного вызова).
 *
 * @see KeycloakClientLoggingUtils
 * @see RegisterRequestDto
 * @see RestClient
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class KeycloakAdminClient {

    /**
     * Максимальное количество символов в описании ошибки для безопасного логирования.
     * Значение по умолчанию: 200 символов.
     */
    @Value("${keycloak.client.logging.error-body-max-chars:200}")
    private int maxErrorBodyChars;

    /**
     * Объект для сериализации/десериализации JSON при построении тел запросов.
     */
    private final ObjectMapper objectMapper;

    /**
     * REST-клиент для общения с административным API Keycloak.
     * Инъектируется через Spring конфигурацию {@link RestClient}.
     */
    private final RestClient keycloakRestClient;

    /**
     * Создаёт нового пользователя в Keycloak.
     * <p>
     * Формирует JSON-тело запроса из переданного DTO, отправляет POST-запрос
     * на endpoint {@code /users} Keycloak Admin API и извлекает идентификатор
     * созданного пользователя из заголовка {@code Location} ответа.
     * <p>
     * Обработка ошибок:
     * <ul>
     *   <li>{@code 409 Conflict} — выбрасывает {@link UserAlreadyExistsException},
     *       если пользователь с указанным email уже существует.</li>
     *   <li>Любая другая ошибка {@code 4xx/5xx} — выбрасывает
     *       {@link KeycloakInfrastructureException}.</li>
     * </ul>
     *
     * @param dto данные для регистрации пользователя (email, пароль, имя, фамилия)
     * @return UUID созданного пользователя, полученный из заголовка {@code Location}
     * @throws UserAlreadyExistsException если пользователь с таким email уже существует в Keycloak
     * @throws KeycloakInfrastructureException если Keycloak вернул ошибку сервера или клиентской ошибки.
     */
    public UUID createKeycloakUser(RegisterRequestDto dto) {
        ObjectNode body = buildBody(dto);

        ResponseEntity<Void> response = keycloakRestClient
                .post()
                .uri("/users")
                .body(body)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (request, res) -> {

                    String errorBody = new String(res.getBody().readAllBytes(), StandardCharsets.UTF_8);

                    log.error("Keycloak Admin API error. Status: {}, Body: {}", res.getStatusCode(), errorBody);

                    if (res.getStatusCode() == HttpStatus.CONFLICT) {
                        throw new UserAlreadyExistsException("Пользователь с email %s уже существует", dto.email());
                    }

                    throw new KeycloakInfrastructureException("Ошибка интеграции с Keycloak: " + res.getStatusCode()
                    );
                })
                .toBodilessEntity();

        return extractUserIdFromLocation(response);
    }

    /**
     * Удаляет пользователя в Keycloak по идентификатору.
     * <p>
     * Отправляет DELETE-запрос на endpoint {@code /users/{userId}}.
     * Если пользователь не найден (статус {@code 404}), операция считается
     * выполненной успешно — выбрасывается только информационное лог-сообщение.
     * <p>
     * Обработка ошибок:
     * <ul>
     *   <li>{@code 4xx} — логирование с уровнем WARN, затем выбрасывает
     *       {@link KeycloakInfrastructureException}.</li>
     *   <li>{@code 5xx} — логирование с уровнем WARN, затем выбрасывает
     *       {@link KeycloakInfrastructureException}.</li>
     * </ul>
     *
     * @param userId UUID пользователя, которого необходимо удалить
     * @throws KeycloakInfrastructureException если Keycloak вернул ошибку сервера или клиентской ошибки (кроме 404)
     */
    public void deleteKeycloakUser(String userId) {
        keycloakRestClient.delete()
                .uri("/users/{userId}", userId)
                .retrieve()
                .onStatus(HttpStatus.NOT_FOUND::equals, (request, response) ->
                        log.info("Keycloak: user not found, delete skipped. userId={}", userId))
                .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                    String summary = extractSafeErrorSummary(response, maxErrorBodyChars);
                    log.warn("Keycloak client error on delete: userId={}, status={}, error={}",
                            userId, response.getStatusCode(), summary);
                    throw new KeycloakInfrastructureException("Keycloak error: " + response.getStatusCode());
                })
                .onStatus(HttpStatusCode::is5xxServerError, (request, response) -> {
                    String summary = extractSafeErrorSummary(response, maxErrorBodyChars);
                    log.warn("Keycloak server error on delete: userId={}, status={}, error={}",
                            userId, response.getStatusCode(), summary);
                    throw new KeycloakInfrastructureException("Keycloak error: " + response.getStatusCode());
                });

    }

    /**
     * Формирует JSON-тело запроса для создания пользователя на основе DTO.
     * <p>
     * Включает основные данные пользователя (email, имя, фамилия, флаг активности)
     * и учётные данные для аутентификации (пароль, помеченный как не временный).
     * Email используется одновременно и как email, и как имя пользователя.
     *
     * @param dto данные для регистрации пользователя
     * @return {@link ObjectNode} с готовым JSON-телом запроса для Keycloak Admin API
     */
    private ObjectNode buildBody(RegisterRequestDto dto) {
        ObjectNode bodyJson = objectMapper.createObjectNode();
        bodyJson.put("email", dto.email());
        bodyJson.put("username", dto.email());
        bodyJson.put("firstName", dto.firstName());
        bodyJson.put("lastName", dto.lastName());
        bodyJson.put("enabled", true);

        ObjectNode credentials = objectMapper.createObjectNode();
        credentials.put("type", "password");
        credentials.put("value", dto.password());
        credentials.put("temporary", false);

        bodyJson.set("credentials", objectMapper.createArrayNode().add(credentials));

        return bodyJson;
    }

    /**
     * Извлекает UUID пользователя из заголовка {@code Location} ответа Keycloak.
     * <p>
     * Парсит последний сегмент пути из {@code Location} как UUID.
     * Если парсинг не удался, запись производится в лог, а метод
     * выбрасывает {@link KeycloakInfrastructureException}.
     *
     * @param response HTTP-ответ от Keycloak со статусом 201 Created
     * @return UUID созданного пользователя
     * @throws KeycloakInfrastructureException если заголовок {@code Location} отсутствует,
     *         его путь нулевой или последний сегмент не является валидным UUID
     */
    private UUID extractUserIdFromLocation(ResponseEntity<Void> response) {
        URI location = response.getHeaders().getLocation();
        if (location != null && location.getPath() != null) {
            String path = location.getPath();
            String userIdString = path.substring(path.lastIndexOf("/") + 1);
            try {
                return UUID.fromString(userIdString);
            } catch (IllegalArgumentException e) {
                log.error("Failed to parse UUID from Location header: {}", path, e);
            }
        }
        throw new KeycloakInfrastructureException("Keycloak вернул 201 Created, но заголовок Location отсутствует или невалиден");
    }
}
