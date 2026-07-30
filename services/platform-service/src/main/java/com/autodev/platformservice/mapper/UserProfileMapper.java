package com.autodev.platformservice.mapper;

import com.autodev.platformservice.dto.UserProfileResponseDto;
import com.autodev.platformservice.entity.UserProfileEntity;
import org.mapstruct.Mapper;

@Mapper
public interface UserProfileMapper {

    UserProfileResponseDto toDto (UserProfileEntity entity);

}
