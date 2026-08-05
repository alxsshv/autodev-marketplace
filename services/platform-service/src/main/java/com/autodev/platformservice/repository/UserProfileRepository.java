package com.autodev.platformservice.repository;

import com.autodev.platformservice.entity.UserProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * JPA-репозиторий для операций с таблицей профилей пользователей.
 * <p>
 * Предоставляет методы для сохранения, обновления и чтения профилей пользователей
 * из реляционной базы данных.
 * <p>
 * <b>Основной сценарий использования:</b>
 * <ul>
 *   <li>{@link #findByKeycloakUserId(String)} — поиск профиля по идентификатору
 *       пользователя в Keycloak. Используется всеми сервисами для идентификации
 *       пользователя в рамках системы.</li>
 *   <li>Наследуемые методы {@link JpaRepository}: {@code save}, {@code findById},
 *       {@code deleteById}, {@code findAll}.</li>
 * </ul>
 *
 * @see UserProfileEntity
 * @see com.autodev.platformservice.service.UserProfileService
 */
@Repository
public interface UserProfileRepository extends JpaRepository<UserProfileEntity, UUID> {

    /**
     * Находит профиль пользователя по его идентификатору в Keycloak.
     * <p>
     * Основной метод для поиска профиля по аутентифицированному пользователю.
     * Идентификатор извлекается из JWT-токена (claim {@code sub}) и передаётся сюда.
     * <p>
     * <b>Пример использования:</b>
     * <pre>{@code
     * String keycloakUserId = SecurityUtils.getCurrentUserId();
     * Optional<UserProfileEntity> profile = userProfileRepository.findByKeycloakUserId(keycloakUserId);
     * }</pre>
     *
     * @param keycloakUserId идентификатор пользователя в Keycloak (claim {@code sub} из JWT)
     * @return Optional с профилем пользователя, если найден, или пустой Optional
     */
    Optional<UserProfileEntity> findByKeycloakUserId(String keycloakUserId);

}
