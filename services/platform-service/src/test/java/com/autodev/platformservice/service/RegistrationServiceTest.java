package com.autodev.platformservice.service;

import com.autodev.platformservice.client.keycloak.KeycloakAdminClient;
import com.autodev.platformservice.dto.RegisterRequestDto;
import com.autodev.platformservice.dto.event.UserRegisteredEventPayload;
import com.autodev.platformservice.entity.UserEvents;
import com.autodev.platformservice.exception.RegistrationOperationException;
import com.autodev.platformservice.exception.UserAlreadyExistsException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.UUID;
import java.util.function.Consumer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegistrationServiceTest {

    @Mock
    private KeycloakAdminClient keycloakAdminClient;
    @Mock
    private TransactionTemplate transactionTemplate;
    @Mock
    private UserProfileService userProfileService;
    @Mock
    private OutboxService outboxService;

    @InjectMocks
    private RegistrationService registrationService;


    private final RegisterRequestDto validDto = new RegisterRequestDto(
            "test@test.com",
            "password123!",
            "Ivan",
            "Ivanov");



    @Test
    @DisplayName("Регистрация: успешная — компенсация не вызывается")
    void testRegisterUser_whenSuccess_shouldNotCallCompensation() {
        UUID keycloakUserId = UUID.randomUUID();

        UserRegisteredEventPayload eventPayload = new UserRegisteredEventPayload(
                keycloakUserId.toString(),
                validDto.email(),
                validDto.firstName(),
                validDto.lastName()
        );

        when(keycloakAdminClient.createKeycloakUser(validDto)).thenReturn(keycloakUserId);

        doAnswer(invocation -> {
            Consumer<?> consumer = invocation.getArgument(0);
            consumer.accept(null);
            return null;
        }).when(transactionTemplate).executeWithoutResult(any(Consumer.class));

        registrationService.registerUser(validDto);

        verify(keycloakAdminClient, never()).deleteKeycloakUser(keycloakUserId.toString());
        verify(userProfileService, times(1))
                .createProfileIfNotExists(keycloakUserId.toString(), validDto.email());
        verify(outboxService, times(1)).publishEvent(UserEvents.USER_REGISTERED, keycloakUserId, eventPayload);
    }

    @Test
    @DisplayName("Регистрация: ошибка БД — запускается компенсация")
    void registerUser_whenDbFails_shouldTriggerCompensation() {
        // Настройка: Keycloak возвращает ID
        UUID keycloakId = UUID.randomUUID();
        when(keycloakAdminClient.createKeycloakUser(validDto)).thenReturn(keycloakId);

        // Настройка: Транзакция падает (имитация ошибки БД)
        doThrow(new RuntimeException("Connection refused")).when(transactionTemplate).executeWithoutResult(any(Consumer.class));

        // Вызов и проверка
        org.junit.jupiter.api.Assertions.assertThrows(RegistrationOperationException.class, () -> {
            registrationService.registerUser(validDto);
        });

        // Проверка: Компенсация ВЫЗВАНА
        verify(keycloakAdminClient).deleteKeycloakUser(keycloakId.toString());
    }

    @Test
    @DisplayName("Регистрация: ошибка Keycloak — компенсация не нужна")
    void registerUser_whenKeycloakFails_shouldNotTriggerCompensation() {
        // Настройка: Keycloak сразу падает
        when(keycloakAdminClient.createKeycloakUser(validDto))
                .thenThrow(new UserAlreadyExistsException("Уже существует"));

        // Вызов и проверка
        org.junit.jupiter.api.Assertions.assertThrows(UserAlreadyExistsException.class, () -> {
            registrationService.registerUser(validDto);
        });

        // Проверка: Транзакция НЕ начиналась, компенсация НЕ вызвана
        verify(transactionTemplate, never()).executeWithoutResult(any());
        verify(keycloakAdminClient, never()).deleteKeycloakUser(anyString());
    }
}



