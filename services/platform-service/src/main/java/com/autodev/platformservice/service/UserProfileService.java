package com.autodev.platformservice.service;

import com.autodev.platformservice.dto.UserProfileResponseDto;
import com.autodev.platformservice.entity.UserProfileEntity;
import com.autodev.platformservice.repository.UserProfileRepository;
import com.autodev.platformservice.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserProfileService {

    private final UserProfileRepository userProfileRepository;

    public UserProfileResponseDto getCurrentUserProfile() {
        String keycloakUserId = SecurityUtils.getCurrentUserId();
        Optional<UserProfileEntity> userProfileOpt = userProfileRepository.findByKeycloakUserId(keycloakUserId);
        if (userProfileOpt.isEmpty()) {

        }

    }

    private UserProfileResponseDto createProfileIfNotExists(String keycloakUserId, String email) {
        log.info("User profile not found for keycloakUserId: {}. Starting creating new prifile", keycloakUserId);

        UserProfileEntity userProfile = UserProfileEntity.builder()
                .build()


    }

}
