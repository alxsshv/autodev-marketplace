package com.autodev.auth.repository;

import com.autodev.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


/** Репозиторий для пользователей {@link User} */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /** Метод поиска пользователей по KeycloakUserId.
     * @param keycloakUserId  - уникальный идентификатор пользователя в keycloak*/
    Optional<User> findByKeycloakUserId(String keycloakUserId);


    /** Метод поиска пользователей по email.
     * @param email - адрес электронной посты пользователя */
    Optional<User> findByEmail(String email);

}
