package com.autodev.auth.mapper;

import com.autodev.auth.dto.shared.UserDto;
import com.autodev.auth.entity.User;
import org.springframework.stereotype.Component;

/**
 * Класс для преобразования объектов передачи данных {@link UserDto} в сущность {@link User} и обратно
 */
@Component
public class UserMapper {


    /** Метод преобразования сущности {@link User} в {@link UserDto} */
    public UserDto toDto(User user) {
        if (user == null) {
            return null;
        }
        return new UserDto(
                user.getId(),
                user.getKeycloakUserId(),
                user.getEmail(),
                user.isEnabled(),
                user.getCreatedAt());
    }

    /** Метод преобразования объекта передачи данных {@link UserDto} в {@link User} */
    public User toEntity(UserDto userDto) {
        if (userDto == null) {
            return null;
        }
        return User.builder()
                .id(userDto.id())
                .keycloakUserId(userDto.keycloakUserId())
                .email(userDto.email())
                .enabled(userDto.enabled())
                .createdAt(userDto.createdAt())
                .build();
    }




}
