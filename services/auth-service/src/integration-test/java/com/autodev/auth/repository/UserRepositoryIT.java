package com.autodev.auth.repository;

import com.autodev.auth.entity.User;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.jdbc.JdbcTestUtils;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@DataJpaTest
@Testcontainers
@SuppressWarnings({"java:S6813", "java:S100", "java:S5960"})
public class UserRepositoryIT {

    @Container
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:15-alpine")
            .withInitScript("init-schemas.sql");

    @DynamicPropertySource
    public static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final User user1 = User.builder()
            .id(1L)
            .keycloakUserId(UUID.randomUUID().toString())
            .email("user1@mail.com")
            .enabled(true)
            .createdAt(LocalDateTime.now())
            .build();


    @BeforeEach
    public void createUser() {
        userRepository.save(user1);
    }

    @AfterEach
    public void clearUsersTable() {
        JdbcTestUtils.dropTables(jdbcTemplate, "auth.users");
    }


    @Test
    @DisplayName("Test findByKeycloakId method with valid keycloakId should return valid user")
    void findByKeycloakId_withValidKeycloakId_shouldReturnValidUser() {
        Optional<User> userOpt = userRepository.findByKeycloakUserId(user1.getKeycloakUserId());

        Assertions.assertTrue(userOpt.isPresent());
        Assertions.assertEquals(user1.getKeycloakUserId(), userOpt.get().getKeycloakUserId());
    }

    @Test
    @DisplayName("Test findByKeycloakId method with invalid keycloakId should return empty optional")
    void findByKeycloakId_withInvalidKeycloakId_shouldReturnEmptyOptional() {

        Optional<User> userOpt = userRepository.findByKeycloakUserId("invalid_keycloak_id");

        Assertions.assertFalse(userOpt.isPresent());
    }

    @Test
    @DisplayName("Test findByEmail method with valid email should return valid user")
    void findByEmail_withValidEmail_shouldReturnValidUser() {

        Optional<User> userOpt = userRepository.findByEmail(user1.getEmail());

        Assertions.assertTrue(userOpt.isPresent());
        Assertions.assertEquals(user1.getEmail(), userOpt.get().getEmail());
    }

    @Test
    @DisplayName("Test findByEmail method with invalid email should return empty optional")
    void findByEmail_withInvalidEmail_shouldReturnEmptyOptional() {

        Optional<User> userOpt = userRepository.findByEmail("notFoundUser@mail.com");

        Assertions.assertTrue(userOpt.isEmpty());
    }

}
