package com.autodev.platformservice.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequestDto(


        @Size(max = 255,
              message = "Превышена максимально допустимая длина названия магазина (255 символов)")
        String StoreName,

        @Size(max = 2000,
              message = "Максимальная длина описания магазина не должна превышать 2000 символов")
        String storeDescription,

        @Pattern(regexp = "^\\+?[0-9\\-\\s]{7,15}$", message = "Неверный формат номер телефона")
        @Size(max = 20, message = "Превышена максимально допустимая длина номера телефона (20 символов)")
        String phone

) {
}
