package com.autodev.auth.dto.request;

import jakarta.validation.constraints.NotBlank;


/**DTO для обновления access токена по refresh токену.
 * @param refreshToken - токен, предоставляемый клиентом, для обновления access токена.
 * */
public record RefreshRequest(

        @NotBlank
        String refreshToken
) {
}
