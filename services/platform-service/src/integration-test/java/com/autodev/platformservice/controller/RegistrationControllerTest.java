package com.autodev.platformservice.controller;

import com.autodev.platformservice.dto.RegisterRequestDto;
import com.autodev.platformservice.exception.KeycloakInfrastructureException;
import com.autodev.platformservice.exception.UserAlreadyExistsException;
import com.autodev.platformservice.service.RegistrationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RegistrationController.class)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings({"java:S100", "java:S5960", "java:S6813", "java:S1192"})
@AutoConfigureMockMvc(addFilters = false)
public class RegistrationControllerTest extends AbstractControllerTest {

    private static final String REGISTRATION_URL = "/api/v1/platform/registration";

    @MockitoBean
    private RegistrationService registrationService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void register_happyPath_shouldReturn201() throws Exception {

        Mockito.doNothing().when(registrationService).registerUser(any(RegisterRequestDto.class));

        RegisterRequestDto dto = new RegisterRequestDto("test@test.com", "Password123!","Иван", "Иванов");

        mockMvc.perform(post(REGISTRATION_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated()); // Ожидаем 201
    }

    @Test
    void register_whenEmailIsBlank_shouldReturn400() throws Exception {
        // Отправляем невалидный JSON (пустой email)
        String invalidJson = """
                {
                  "firstName": "Иван",
                  "lastName": "Иванов",
                  "email": "",
                  "password": "123"
                }
                """;

        mockMvc.perform(post(REGISTRATION_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest()); // Ожидаем 400 из-за @NotBlank
    }

    @Test
    void register_whenEmailIsInvalid_shouldReturn400() throws Exception {
        String invalidJson = """
                {
                  "firstName": "Иван",
                  "email": "not-an-email",
                  "password": "Password123!"
                }
                """;

        mockMvc.perform(post(REGISTRATION_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest()); // Ожидаем 400 из-за @Email
    }

    @Test
    void register_whenUserAlreadyExists_shouldReturn409() throws Exception {
        // Настраиваем мок: сервис кидает исключение (имитация ответа 409 от Keycloak)
        Mockito.doThrow(new UserAlreadyExistsException("Пользователь с email %s уже существует", "test@test.com"))
                .when(registrationService).registerUser(any(RegisterRequestDto.class));

        RegisterRequestDto dto = new RegisterRequestDto( "test@test.com", "Password123!", "Иван", "Иванов");

        mockMvc.perform(post(REGISTRATION_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict()); // Ожидаем 409
    }

    @Test
    void register_whenKeycloakIsDown_shouldReturn503() throws Exception {
        Mockito.doThrow(new KeycloakInfrastructureException("Ошибка интеграции с Keycloak"))
                .when(registrationService).registerUser(any(RegisterRequestDto.class));

        RegisterRequestDto dto = new RegisterRequestDto("test@test.com", "Password123!", "Иван", "Иванов");

        mockMvc.perform(post(REGISTRATION_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isServiceUnavailable()); // Ожидаем 503
    }
}


