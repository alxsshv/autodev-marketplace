package com.autodev.platformservice.service;

import com.autodev.platformservice.AbstractPlatformIntegrationTest;
import com.autodev.platformservice.dto.RegisterRequestDto;
import com.autodev.platformservice.entity.OutboxEntity;
import com.autodev.platformservice.entity.OutboxStatus;
import com.autodev.platformservice.entity.UserProfileEntity;
import com.autodev.platformservice.entity.VerificationStatus;
import com.autodev.platformservice.repository.OutboxRepository;
import com.autodev.platformservice.repository.UserProfileRepository;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.util.List;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings({"java:S100", "java:S5960", "java:S6813"})
@WireMockTest(httpPort = 8099)
public class RegistrationServiceIntegrationTest extends AbstractPlatformIntegrationTest {

    static final String KEYCLOAK_URL = "http://localhost:8099";

    @Autowired
    private RegistrationService registrationService;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private OutboxRepository outboxRepository;


    @DynamicPropertySource
    static void registerKeycloakWiremock(DynamicPropertyRegistry registry) {
        registry.add("keycloak.admin.server-url", () -> KEYCLOAK_URL);
    }

    @AfterEach
    void cleanUp() {
        userProfileRepository.deleteAll();
        outboxRepository.deleteAll();
    }

    @Test
    void registerUser_shouldCreateProfileAndOutboxEvent() {
        UUID keycloakId = UUID.randomUUID();

        stubFor(post(urlEqualTo("/realms/autodev/protocol/openid-connect/token"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                    {
                      "access_token": "fake-test-access-token",
                      "token_type": "Bearer",
                      "expires_in": 300
                    }
                    """)
                ));

        stubFor(WireMock
                .post(urlEqualTo("/admin/realms/autodev/users"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.CREATED.value())
                        .withHeader("Location", KEYCLOAK_URL + "/admin/realms/autodev/users/" + keycloakId)));

        RegisterRequestDto registerDto = new RegisterRequestDto(
                "test@test.com",
                "Password123!",
                "Иван",
                "Иванов"
                );

        registrationService.registerUser(registerDto);


        List<UserProfileEntity> profiles = userProfileRepository.findAll();
        assertEquals(1, profiles.size());
        UserProfileEntity profile = profiles.get(0);


        Pageable pageable = PageRequest.of(0, 100);
        List<OutboxEntity> outboxEvents = outboxRepository.findByStatusOrderByCreatedAtAsc(OutboxStatus.PENDING, pageable);

        Assertions.assertAll(
                () -> assertEquals(keycloakId.toString(), profile.getKeycloakUserId()),
                () -> assertEquals(registerDto.email(), profile.getEmail()),
                () -> assertEquals(VerificationStatus.NOT_VERIFIED, profile.getVerificationStatus()),
                () -> assertEquals(1, outboxEvents.size()),
                () -> assertEquals("UserProfile", outboxEvents.get(0).getAggregateType()),
                () -> assertEquals(keycloakId, outboxEvents.get(0).getAggregateId()),
                () -> assertTrue(outboxEvents.get(0).getPayload().contains(registerDto.email()))
        );



    }

}
