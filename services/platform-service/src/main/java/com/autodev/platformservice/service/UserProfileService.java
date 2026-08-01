package com.autodev.platformservice.service;

import com.autodev.platformservice.dto.UpdateProfileRequestDto;
import com.autodev.platformservice.dto.UserProfileResponseDto;
import com.autodev.platformservice.entity.UserProfileEntity;
import com.autodev.platformservice.entity.VerificationStatus;
import com.autodev.platformservice.exception.UserProfileNotFoundException;
import com.autodev.platformservice.mapper.UserProfileMapper;
import com.autodev.platformservice.repository.UserProfileRepository;
import com.autodev.platformservice.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Сервис для управления профилями пользователей платформы.
 * <p>
 * Предоставляет бизнес-логику для:
 * <ul>
 *   <li>Получения профиля текущего аутентифицированного пользователя
 *       ({@link #getCurrentUserProfile()}).</li>
 *   <li>Создания нового профиля, если он ещё не существует
 *       ({@link #createProfileIfNotExists(String, String)}).</li>
 *   <li>Обновления данных профиля ({@link #updateUserProfile(UpdateProfileRequestDto, String)}).</li>
 * </ul>
 * <p>
 * <b>Lazy-создание профиля:</b> метод {@link #getCurrentUserProfile()}
 * автоматически создаёт новый профиль, если он не найден по
 * {@code keycloakUserId}. Это позволяет пользователям взаимодействовать
 * с платформой без явного предварительного создания профиля.
 * <p>
 * <b>Гонка конкуренции:</b> при параллельных запросах от одного пользователя
 * может возникнуть {@link DataIntegrityViolationException} (уникальный
 * индекс по {@code keycloakUserId}). В этом случае сервис выполняет
 * повторную попытку поиска профиля (retry pattern).
 * <p>
 * <b>Потокобезопасность:</b> сервис не хранит изменяемое состояние,
 * все операции выполняются через инъектированные репозитории и мапперы,
 * которые также являются потокобезопасными.
 *
 * @see UserProfileEntity
 * @see UserProfileResponseDto
 * @see UserProfileMapper
 * @see UserProfileRepository
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserProfileService {

    /**
     * Репозиторий для хранения и загрузки профилей пользователей из БД.
     * Инъектируется через Spring.
     */
    private final UserProfileRepository userProfileRepository;

    /**
     * MapStruct-маппер для преобразования между {@link UserProfileEntity} и DTO.
     * Инъектируется через Spring (сгенерированная реализация).
     */
    private final UserProfileMapper userProfileMapper;

    /**
     * Получает профиль текущего аутентифицированного пользователя.
     * <p>
     * <b>Алгоритм:</b>
     * <ol>
     *   <li>Извлекает {@code keycloakUserId} из текущего контекста аутентификации
     *       через {@link SecurityUtils#getCurrentUserId()}.</li>
     *   <li>Ищет профиль в БД по {@code keycloakUserId}.</li>
     *   <li>Если профиль не найден — автоматически создаёт новый с базовыми значениями
     *       (статус {@code NOT_VERIFIED}, баланс {@code BigDecimal.ZERO}).</li>
     *   <li>В случае гонки конкуренции (DataIntegrityViolationException) выполняет
     *       повторную попытку поиска (retry).</li>
     *   <li>Преобразует сущность в DTO через {@link UserProfileMapper#toDto(UserProfileEntity)}.</li>
     * </ol>
     *
     * @return профиль текущего пользователя
     * @throws UserProfileNotFoundException если профиль не найден и создать его невозможно
     */
    @SuppressWarnings("java:S6809")
    @Transactional(propagation = Propagation.REQUIRED)
    public UserProfileResponseDto getCurrentUserProfile() {
        String keycloakUserId = SecurityUtils.getCurrentUserId();
        Optional<UserProfileEntity> userProfileOpt = userProfileRepository.findByKeycloakUserId(keycloakUserId);

        UserProfileEntity userProfile = userProfileOpt.orElseGet(() -> {
            try {
                log.info("User profile for userId {} not found. Started creating user profile", keycloakUserId);
                return createProfileIfNotExists(keycloakUserId, SecurityUtils.getCurrentUserEmail());
            } catch (DataIntegrityViolationException ex) {
                log.warn("Failed creating user profile for userId {} : {}",keycloakUserId, ex.getMessage());
                log.info("A second attempt was made to find the user profile by userId {}", keycloakUserId);
                Optional<UserProfileEntity> userProfileOptional = userProfileRepository.findByKeycloakUserId(keycloakUserId);
                return userProfileOptional.orElseThrow(
                        () -> new UserProfileNotFoundException("User profile by userId %s не найден", keycloakUserId));

            }
        });

        return userProfileMapper.toDto(userProfile);
    }

    /**
     * Создаёт новый профиль пользователя, если он ещё не существует.
     * <p>
     * <b>Базовые значения профиля:</b>
     * <ul>
     *   <li>{@code keycloakUserId} — идентификатор из Keycloak.</li>
     *   <li>{@code email} — адрес электронной почты.</li>
     *   <li>{@code verificationStatus} — {@link VerificationStatus#NOT_VERIFIED}.</li>
     *   <li>{@code loyaltyBalance} — {@code 0}.</li>
     * </ul>
     * <p>
     * Этот метод вызывается:
     * <ul>
     *   <li>При регистрации пользователя (через {@link RegistrationService}).</li>
     *   <li>При первом запросе текущего профиля, если он не найден
     *       (lazy-создание в {@link #getCurrentUserProfile()}).</li>
     * </ul>
     *
     * @param keycloakUserId идентификатор пользователя в Keycloak
     * @param email адрес электронной почты пользователя
     * @return созданный профиль пользователя
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public UserProfileEntity createProfileIfNotExists(String keycloakUserId, String email) {
        log.info("User profile not found for keycloakUserId: {}. Starting creating new profile", keycloakUserId);

        UserProfileEntity userProfile = UserProfileEntity.builder()
                .keycloakUserId(keycloakUserId)
                .email(email)
                .verificationStatus(VerificationStatus.NOT_VERIFIED)
                .loyaltyBalance(BigDecimal.ZERO)
                .build();

        userProfileRepository.save(userProfile);
        return userProfile;
    }

    /**
     * Обновляет данные профиля указанного пользователя.
     * <p>
     * <b>Обновляемые поля:</b>
     * <ul>
     *   <li>Название магазина ({@code storeName}).</li>
     *   <li>Описание магазина ({@code storeDescription}).</li>
     *   <li>Номер телефона ({@code phone}).</li>
     * </ul>
     * <p>
     * <b>Необновляемые поля (через этот метод):</b>
     * email, {@code keycloakUserId}, {@code verificationStatus},
     * {@code loyaltyBalance}, {@code createdAt}, {@code updatedAt}.
     * <p>
     * <b>Алгоритм:</b>
     * <ol>
     *   <li>Находит профиль по {@code keycloakUserId}.</li>
     *   <li>Если не найден — выбрасывает {@link UserProfileNotFoundException}.</li>
     *   <li>Обновляет доступные поля через {@link UserProfileMapper#updateEntityFromDto(UpdateProfileRequestDto, UserProfileEntity)}.</li>
     *   <li>Сохраняет обновлённую сущность в БД.</li>
     *   <li>Возвращает обновлённый DTO.</li>
     * </ol>
     *
     * @param dto данные для обновления
     * @param keycloakUserId идентификатор пользователя в Keycloak
     * @return обновлённый профиль пользователя
     * @throws UserProfileNotFoundException если профиль не найден
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public UserProfileResponseDto updateUserProfile(UpdateProfileRequestDto dto, String keycloakUserId) {
        log.info("Updating profile for keycloak userId = {}", keycloakUserId);

        UserProfileEntity userProfile = userProfileRepository
                .findByKeycloakUserId(keycloakUserId)
                .orElseThrow(() -> new UserProfileNotFoundException("User profile not found by userId = %s", keycloakUserId));
        userProfileMapper.updateEntityFromDto(dto, userProfile);
        userProfileRepository.save(userProfile);
        return userProfileMapper.toDto(userProfile);
    }

}
