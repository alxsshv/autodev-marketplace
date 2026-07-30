package com.autodev.platformservice.client.keycloak;

import org.springframework.http.client.ClientHttpResponse;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Набор утилитных методов для безопасного логирования ответов клиента Keycloak.
 * <p>
 * Предназначен для извлечения кратких описаний ошибок из тела HTTP-ответа
 * без риска раскрытия конфиденциальных данных. Ограничивает объём читаемых
 * данных (не более 1 КБ) и не допускает выбросов исключений — в случае любой
 * ошибки возвращается безопасное текстовое представление.
 * <p>
 * Все методы класса являются потокобезопасными, поскольку не хранят
 * внутреннее состояние. Класс не может быть инстанцирован (private конструктор).
 *
 * <h3>Основные возможности</h3>
 * <ul>
 *   <li>Чтение тела ответа с ограничением по размеру (1 КБ).</li>
 *   <li>Извлечение значений полей {@code error} и {@code error_description}
 *       из JSON-ответа Keycloak без использования полноценного парсера.</li>
 *   <li>Возврат безопасного fallback-значения при невозможности чтения.</li>
 * </ul>
 *
 * @see KeycloakAdminClient
 */
public class KeycloakClientLoggingUtils {

    /** Максимальное количество байт тела ответа для чтения (1 КБ). */
    private static final int MAX_RESPONSE_BODY_SIZE = 1024;

    /** Список имён полей, которые считаются полезными при извлечении описания ошибки. */
    private static final List<String> VALID_ERROR_FIELD_NAMES = List.of("error", "error_description");

    /**
     * Приватный конструктор для предотвращения инстанцирования утилитного класса.
     */
    private KeycloakClientLoggingUtils() {
    }

    /**
     * Извлекает краткое, безопасное для логирования описание ошибки из HTTP-ответа Keycloak.
     * <p>
     * Метод читает не более 1 КБ тела ответа и пытается найти в JSON-содержимом
     * поля {@code error} или {@code error_description}. Если ни одно из полей
     * не найдено, возвращаются первые символы тела ответа. В случае любой
     * ошибки чтения возвращается заглушка {@code "unable to read error body"}.
     * <p>
     * <b>Важно:</b> метод гарантированно не выбрасывает исключения, что делает
     * его безопасным для использования в блоках {@code onStatus} обработчиков ошибок.
     *
     * @param response HTTP-ответ от Keycloak, из которого извлекается информация об ошибке
     * @param maxErrorBodyChars максимальное количество символов для fallback-режима
     *                          (когда поля error/error_description не найдены)
     * @return краткое описание ошибки или безопасный fallback-текст
     */
    public static String extractSafeErrorSummary(ClientHttpResponse response, int maxErrorBodyChars) {
        try {
            byte[] bytes = response.getBody().readNBytes(MAX_RESPONSE_BODY_SIZE);   // ограничиваем размер
            String body = new String(bytes, StandardCharsets.UTF_8).trim();
            if (body.isEmpty()) {
                return "empty body";
            }
            // Пытаемся извлечь понятное поле из JSON, типичного для Keycloak
            return extractJsonTextField(body, maxErrorBodyChars);
        } catch (Exception e) {
            return "unable to read error body";
        }
    }

    /**
     * Извлекает значение первого найденного поля ошибки из JSON-строки.
     * <p>
     * Использует простую строковую поиск по известным именам полей
     * ({@code "error"}, {@code "error_description"}) без подключения
     * полноценного JSON-парсера для минимизации накладных расходов.
     *
     * @param json исходная JSON-строка для анализа
     * @param maxErrorBodyChars максимальная длина возвращаемой строки в режиме fallback
     * @return значение найденного поля или обрезанное тело ответа (первые {@code maxErrorBodyChars} символов)
     */
    private static String extractJsonTextField(String json, int maxErrorBodyChars) {
        for (String field : VALID_ERROR_FIELD_NAMES) {
            String search = "\"" + field + "\":\"";
            int start = json.indexOf(search);
            if (start != -1) {
                start += search.length();
                int end = json.indexOf("\"", start);
                if (end != -1) {
                    return json.substring(start, end);
                }
            }
        }
        return json.length() > maxErrorBodyChars ? json.substring(0, maxErrorBodyChars) + "..." : json;
    }

}
