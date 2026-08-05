package com.autodev.platformservice.service;

import com.autodev.platformservice.dto.UpdateProfileRequestDto;
import com.autodev.platformservice.dto.UserProfileResponseDto;
import com.autodev.platformservice.entity.UserProfileEntity;
import com.autodev.platformservice.entity.VerificationStatus;
import com.autodev.platformservice.exception.UserProfileNotFoundException;
import com.autodev.platformservice.mapper.UserProfileMapper;
import com.autodev.platformservice.repository.UserProfileRepository;
import com.autodev.platformservice.security.SecurityUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserProfileServiceTest {

    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private UserProfileMapper userProfileMapper;

    @InjectMocks
    private UserProfileService userProfileService;

    // Обертка для мокирования статических методов
    private MockedStatic<SecurityUtils> securityUtilsMock;

    private static final String KEYCLOAK_ID = "uuid-123";
    private static final String EMAIL = "test@test.com";

    private final UserProfileResponseDto expectedDto = new UserProfileResponseDto(
            UUID.randomUUID(),
            "test@test.com",
            UUID.randomUUID().toString(),
            "storeName",
            "storeDescription",
            "logoUrl",
            "avatarUrl",
            VerificationStatus.NOT_VERIFIED.name(),
            BigDecimal.ZERO,
            OffsetDateTime.now().minusDays(1),
            OffsetDateTime.now().minusHours(5)
    );

    @BeforeEach
     void setUp() {
        // Открываем статический мок перед каждым тестом
        securityUtilsMock = mockStatic(SecurityUtils.class);
        securityUtilsMock.when(SecurityUtils::getCurrentUserId).thenReturn(KEYCLOAK_ID);
        securityUtilsMock.when(SecurityUtils::getCurrentUserEmail).thenReturn(EMAIL);
    }

    @AfterEach
    void tearDown() {
        // ОБЯЗАТЕЛЬНО закрываем статический мок, иначе он утечет в другой тест
        if (securityUtilsMock != null) {
            securityUtilsMock.close();
        }
    }

    @Test
    @DisplayName("Получение профиля: найдён в БД — возвращает DTO")
    void getCurrentUserProfile_whenExists_shouldReturnDto() {
        // Настройка: Профиль найден в БД
        UserProfileEntity entity = createMockEntity();

        when(userProfileRepository.findByKeycloakUserId(KEYCLOAK_ID)).thenReturn(Optional.of(entity));
        when(userProfileMapper.toDto(entity)).thenReturn(expectedDto);

        // Вызов
        UserProfileResponseDto result = userProfileService.getCurrentUserProfile();

        // Проверка
        assertNotNull(result);
        assertEquals(expectedDto, result);
        // Self-healing НЕ должен был запускаться
        verify(userProfileRepository, never()).save(any());
    }

    @Test
    @DisplayName("Получение профиля: не найден — создаётся автоматически")
    void getCurrentUserProfile_whenNotExists_shouldTriggerSelfHealing() {
        // Настройка: Профиль НЕ найден
        when(userProfileRepository.findByKeycloakUserId(KEYCLOAK_ID)).thenReturn(Optional.empty());

        // Готовимся перехватить то, что сохранится в БД
        ArgumentCaptor<UserProfileEntity> captor = ArgumentCaptor.forClass(UserProfileEntity.class);
        when(userProfileRepository.save(captor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        when(userProfileMapper.toDto(any(UserProfileEntity.class))).thenReturn(expectedDto);

        // Вызов
        UserProfileResponseDto result = userProfileService.getCurrentUserProfile();

        // Проверка
        assertNotNull(result);

        // Проверяем, что сохранилось правильное значение (Self-Healing логика)
        UserProfileEntity savedEntity = captor.getValue();
        assertEquals(KEYCLOAK_ID, savedEntity.getKeycloakUserId());
        assertEquals(EMAIL, savedEntity.getEmail());
        assertEquals(VerificationStatus.NOT_VERIFIED, savedEntity.getVerificationStatus());
        assertEquals(BigDecimal.ZERO, savedEntity.getLoyaltyBalance());

        verify(userProfileRepository).save(any());
    }

    @Test
    @DisplayName("Обновление профиля: найдён — обновляется и возвращает DTO")
    void updateUserProfile_whenExists_shouldUpdateAndReturnDto() {
        // Настройка
        UserProfileEntity entity = createMockEntity();
        UpdateProfileRequestDto dto = new UpdateProfileRequestDto("Новое Имя", "Новое Описание", "url", "storeUrl","avatar");

        when(userProfileRepository.findByKeycloakUserId(KEYCLOAK_ID)).thenReturn(Optional.of(entity));
        when(userProfileMapper.toDto(entity)).thenReturn(expectedDto);

        // Вызов
        UserProfileResponseDto result = userProfileService.updateUserProfile(dto, KEYCLOAK_ID);

        // Проверка
        assertNotNull(result);
        // Проверяем, что маппер обновления был вызван с нужными аргументами
        verify(userProfileMapper).updateEntityFromDto(dto, entity);
    }

    @Test
    @DisplayName("Обновление профиля: не найден — выбрасывает исключение")
    void updateUserProfile_whenNotExists_shouldThrowNotFoundException() {
        // Настройка
        UpdateProfileRequestDto dto = new UpdateProfileRequestDto("Имя", "Описание", "+79857377733", "storeUrl","avatar");
        when(userProfileRepository.findByKeycloakUserId(KEYCLOAK_ID)).thenReturn(Optional.empty());

        // Вызов и проверка
        UserProfileNotFoundException exception = assertThrows(UserProfileNotFoundException.class, () -> {
            userProfileService.updateUserProfile(dto, KEYCLOAK_ID);
        });

        assertTrue(exception.getMessage().contains("не найден") || exception.getMessage().contains("not found"));
        // Маппер обновления НЕ должен был вызываться
        verify(userProfileMapper, never()).updateEntityFromDto(any(), any());
    }

    // Вспомогательный метод
    private UserProfileEntity createMockEntity() {
        UserProfileEntity entity = new UserProfileEntity();
        entity.setKeycloakUserId(KEYCLOAK_ID);
        entity.setEmail(EMAIL);
        return entity;
    }
}

