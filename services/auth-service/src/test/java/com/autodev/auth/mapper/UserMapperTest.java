package com.autodev.auth.mapper;


import com.autodev.auth.dto.shared.UserDto;
import com.autodev.auth.entity.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
public class UserMapperTest {

    private final UserMapper userMapper = new UserMapper();

    private final User testUser = User.builder()
            .id(3L)
            .keycloakUserId(UUID.randomUUID().toString())
            .email("testuser@email.com")
            .enabled(true)
            .createdAt(LocalDateTime.now())
            .build();

    private final UserDto testUserDto = new UserDto(
            1L,
            UUID.randomUUID().toString(),
            "dto@email.com",
            true,
            LocalDateTime.now());


    @Nested
    class ToDtoMethodTests {

        @Test
        @DisplayName("Test toDto method with valid user should return valid userDto")
        void toDto_withValidUser_shouldReturnValidUserDto() {
            UserDto dto = userMapper.toDto(testUser);

            Assertions.assertAll("UserDto checking",
                    () -> Assertions.assertEquals(testUser.getId(), dto.id(), "Invalid id"),
                    () -> Assertions.assertEquals(testUser.getKeycloakUserId(), dto.keycloakUserId(), "Invalid keycloakUserId"),
                    () -> Assertions.assertEquals(testUser.getEmail(), dto.email(), "Invalid email"),
                    () -> Assertions.assertEquals(testUser.isEnabled(), dto.enabled(), "Invalid value from \"enabled\" field"),
                    () -> Assertions.assertEquals(testUser.getCreatedAt(), dto.createdAt(), "Invalid creation date")
            );
        }

        @Test
        @DisplayName("Test toDto method with user is null should return null")
        void toDto_withUserIsNull_shouldReturnNull() {
            UserDto dto = userMapper.toDto(null);

            Assertions.assertNull(dto);
        }

    }

    @Nested
    class ToEntityMethodTests {

        @Test
        @DisplayName("Test toEntity method with valid userDto should return valid user")
        void toEntity_withValidUserDto_shouldReturnValidUser() {
            User user = userMapper.toEntity(testUserDto);

            Assertions.assertAll("User checking",
                    () -> Assertions.assertEquals(testUserDto.id(), user.getId(), "Invalid id"),
                    () -> Assertions.assertEquals(testUserDto.keycloakUserId(),user.getKeycloakUserId(), "Invalid keycloakUserId"),
                    () -> Assertions.assertEquals(testUserDto.email(), user.getEmail(), "Invalid email"),
                    () -> Assertions.assertEquals(testUserDto.enabled(), user.isEnabled(), "Invalid value from \"enabled\" field"),
                    () -> Assertions.assertEquals(testUserDto.createdAt(), user.getCreatedAt(), "Invalid creation date")
            );

        }

        @Test
        @DisplayName("Test toEntity method with userDto is null should return null")
        void toDto_withUserDtoIsNull_shouldReturnNull() {
            User user = userMapper.toEntity(null);

            Assertions.assertNull(user);
        }


    }
}
