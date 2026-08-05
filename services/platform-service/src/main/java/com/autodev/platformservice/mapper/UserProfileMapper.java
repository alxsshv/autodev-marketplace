package com.autodev.platformservice.mapper;

import com.autodev.platformservice.dto.UpdateProfileRequestDto;
import com.autodev.platformservice.dto.UserProfileResponseDto;
import com.autodev.platformservice.entity.UserProfileEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

/**
 * MapStruct-маппер для преобразования между {@link UserProfileEntity} и DTO.
 * <p>
 * MapStruct генерирует реализацию этого интерфейса во время компиляции,
 * обеспечивая типобезопасное и высокопроизводительное преобразование
 * между сущностью JPA и объектами передачи данных (DTO).
 * <p>
 * <b>Доступные маппинги:</b>
 * <ul>
 *   <li>{@link #toDto(UserProfileEntity)} — сущность → {@code UserProfileResponseDto}.</li>
 *   <li>{@link #updateEntityFromDto(UpdateProfileRequestDto, UserProfileEntity)} — частичное обновление сущности из DTO.</li>
 * </ul>
 *
 * @see UserProfileResponseDto
 * @see UpdateProfileRequestDto
 * @see UserProfileEntity
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserProfileMapper {

    /**
     * Преобразует сущность профиля пользователя в DTO ответа.
     * <p>
     * Копирует все поля из {@link UserProfileEntity} в {@link UserProfileResponseDto}.
     * MapStruct автоматически сопоставит поля с одинаковыми именами и типами.
     *
     * @param entity сущность профиля пользователя
     * @return DTO для передачи данных клиенту
     */
    UserProfileResponseDto toDto(UserProfileEntity entity);

    /**
     * Частично обновляет сущность профиля из DTO запроса.
     * <p>
     * Обновляет только поля, доступные для изменения пользователем:
     * {@code storeName}, {@code storeDescription}, {@code phone}.
     * <p>
     * <b>Игнорируемые поля (не меняются через этот метод):</b>
     * <ul>
     *   <li>{@code id} — первичный ключ, не изменяется.</li>
     *   <li>{@code keycloakUserId} — идентификатор в Keycloak, не изменяется.</li>
     *   <li>{@code email} — email меняется только через верификацию/админку.</li>
     *   <li>{@code verificationStatus} — статус меняется только через верификацию/админку.</li>
     *   <li>{@code loyaltyBalance} — баланс меняется только через биллинг.</li>
     *   <li>{@code createdAt} — аудиторское поле, не изменяется.</li>
     *   <li>{@code updatedAt} — Hibernate обновит автоматически через {@code @UpdateTimestamp}.</li>
     * </ul>
     *
     * @param dto данные для обновления
     * @param entity сущность для обновления (изменяется по месту)
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "keycloakUserId", ignore = true)
    @Mapping(target = "email", ignore = true) // Email меняем только через верификацию/админку
    @Mapping(target = "verificationStatus", ignore = true) // Статус меняем только через верификацию/админку
    @Mapping(target = "loyaltyBalance", ignore = true) // Баланс меняется только через биллинг
    @Mapping(target = "createdAt", ignore = true) // Аудит не трогаем
    @Mapping(target = "updatedAt", ignore = true) // Hibernate обновит сам через @UpdateTimestamp
    void updateEntityFromDto(UpdateProfileRequestDto dto, @MappingTarget UserProfileEntity entity);

}
