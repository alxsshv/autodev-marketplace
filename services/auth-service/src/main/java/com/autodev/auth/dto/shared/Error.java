package com.autodev.auth.dto.shared;

import java.time.LocalDateTime;

/** DTO для передачи сведений об ошибке.
 * @param error - тип ошибки,
 * @param message - тест сообщения об ошибке,
 * @param timestamp - время возникновения ошибки
 */
public record Error(
        String error,
        String message,
        LocalDateTime timestamp
) {
}
