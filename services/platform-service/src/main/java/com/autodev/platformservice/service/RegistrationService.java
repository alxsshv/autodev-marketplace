package com.autodev.platformservice.service;

import com.autodev.platformservice.client.keycloak.KeycloakAdminClient;
import com.autodev.platformservice.dto.RegisterRequestDto;
import com.autodev.platformservice.entity.*;
import com.autodev.platformservice.exception.KeycloakInfrastructureException;
import com.autodev.platformservice.exception.RegistrationOperationException;
import com.autodev.platformservice.repository.OutboxRepository;
import com.autodev.platformservice.repository.UserProfileRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RegistrationService {

    private final KeycloakAdminClient keycloakAdminClient;
    private final UserProfileRepository userProfileRepository;
    private final OutboxRepository outboxRepository;
    private final TransactionTemplate transactionTemplate;
    private final ObjectMapper objectMapper;

    public void registerUser(RegisterRequestDto dto) {
        log.info("Starting registration for email: {}", dto.email());

        UUID keycloakUserId = keycloakAdminClient.createKeycloakUser(dto);

        try {
            log.info("User created in Keycloak for email {} with keycloakUserId = {}", dto.email(), keycloakUserId);

            transactionTemplate.executeWithoutResult(status -> {
                saveUserProfile(keycloakUserId, dto);
                saveOutboxEvent(keycloakUserId, dto);
            });
        } catch (Exception ex) {
            log.error("Registration failed for email {}, initiating compensating transaction", dto.email(), ex);
            compensateKeycloakUserCreation(keycloakUserId);
            throw new RegistrationOperationException("Ошибка при создании профиля для пользователя %s: %s", dto.email(), ex.getMessage());
        }
}

    private void saveUserProfile(UUID keycloakUserId, RegisterRequestDto dto) {
        UserProfileEntity userProfile = UserProfileEntity.builder()
                        .keycloakUserId(keycloakUserId.toString())
                        .email(dto.email())
                        .verificationStatus(VerificationStatus.NOT_VERIFIED)
                        .loyaltyBalance(new BigDecimal(0))
                        .build();

        userProfileRepository.save(userProfile);

    }

    private void saveOutboxEvent(UUID keycloakUserId, RegisterRequestDto dto) {
        try {
            String payloadJson = objectMapper.writeValueAsString(
                    Map.of("keycloakUserId", keycloakUserId.toString(), "email", dto.email())
            );

            OutboxEntity event = OutboxEntity.builder()
                    .aggregateType(UserEvents.AGGREGATE_TYPE)
                    .aggregateId(keycloakUserId)
                    .eventType(UserEvents.EventType.USER_REGISTERED)
                    .topic(UserEvents.TOPIC)
                    .payload(payloadJson)
                    .status(OutboxStatus.PENDING)
                    .build();

            outboxRepository.save(event);
        } catch (JsonProcessingException ex) {
            log.info("Failed serialize outbox payload when registration user with email {}: {} ", dto.email(), ex.getMessage());
            throw new RegistrationOperationException("Ошибка регистрации пользователя %s, пожалуйста повторите регистрацию позже", dto.email());
        }
    }


    private void compensateKeycloakUserCreation(UUID keycloakUserId) {
        try {
            keycloakAdminClient.deleteKeycloakUser(keycloakUserId.toString());
            log.info("Compensating transaction: successfully deleted Keycloak user {}", keycloakUserId);
        } catch (KeycloakInfrastructureException ex) {
            //TODO: Нужно добавить метрику, контролирующую сколько ошибок такого рода у нас возникает.
            log.error("COMPENSATION FAILED: Could not delete Keycloak user {} error: {}. Manual intervention required!", keycloakUserId, ex.getMessage());
        }
    }

}
