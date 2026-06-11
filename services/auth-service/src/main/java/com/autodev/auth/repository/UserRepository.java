package com.autodev.auth.repository;

import com.autodev.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByKeycloakUserId(String keycloakUserId);



    @Query("SELECT user FROM users WHERE email = :email")
    User findByEmail(String email);

}
