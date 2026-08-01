package com.autodev.platformservice.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Перечисление типов событий, связанных с пользователем платформы.
 * <p>
 * Определяет набор доменных событий, которые генерируются при различных
 * действиях с профилем пользователя. Каждый тип события реализует
 * {@link DomainEvent} и содержит метаинформацию для маршрутизации:
 * <ul>
 *   <li>Тип агрегата — {@code "UserProfile"} (общий для всех событий пользователя),</li>
 *   <li>Топик — {@code "platform-user-events"} (единый канал для всех событий платформы),</li>
 *   <li>Имя события — уникальная константа перечисления.</li>
 * </ul>
 *
 * <h3>События</h3>
 * <table summary="Список событий пользователя">
 *   <tr>
 *     <th>Событие</th>
 *     <th>Описание</th>
 *   </tr>
 *   <tr>
 *     <td>{@link #USER_REGISTERED}</td>
 *     <td>Пользователь успешно зарегистрирован в системе</td>
 *   </tr>
 *   <tr>
 *     <td>{@link #USER_PROFILE_UPDATED}</td>
 *     <td>Профиль пользователя обновлён (изменены имя, фамилия и т.д.)</td>
 *   </tr>
 *   <tr>
 *     <td>{@link #USER_VERIFIED}</td>
 *     <td>Пользователь прошёл верификацию (подтверждение email или других данных)</td>
 *   </tr>
 * </table>
 *
 * <h3>Использование</h3>
 * <p>
 * Экземпляры {@code UserEvents} передаются в {@link OutboxService#publishEvent(DomainEvent, java.util.UUID, Object)}
 * для сохранения в outbox после совершения соответствующего действия с пользователем.
 * Например, после успешной регистрации пользователя в Keycloak публикуется событие
 * {@code USER_REGISTERED} с полезной нагрузкой (например, {@code RegisterRequestDto}).
 *
 * @see DomainEvent
 * @see OutboxService
 * @see OutboxEntity
 */
@Getter
@RequiredArgsConstructor
public enum UserEvents implements DomainEvent {

    /**
     * Событие успешной регистрации нового пользователя.
     * Генерируется после создания учётной записи в Keycloak.
     */
    USER_REGISTERED("USER_REGISTERED"),

    /**
     * Событие обновления профиля пользователя.
     * Генерируется при изменении персональных данных (имя, фамилия и т.п.).
     */
    USER_PROFILE_UPDATED("USER_PROFILE_UPDATED"),

    /**
     * Событие успешной верификации пользователя.
     * Генерируется после подтверждения email или других идентификационных данных.
     */
    USER_VERIFIED("USER_VERIFIED");

    /**
     * Тип агрегата для всех событий пользователя.
     * <p>
     * Фиксированное значение {@code "UserProfile"}, поскольку все события
     * относятся к одному агрегату — профилю пользователя.
     */
    private final String aggregateType = "UserProfile";

    /**
     * Топик для всех событий платформы, связанных с пользователями.
     * <p>
     * Фиксированное значение {@code "platform-user-events"}, используется
     * для маршрутизации событий消息-брокером.
     */
    private final String topic = "platform-user-events";

    /**
     * Уникальное имя типа события.
     * <p>
     * Идентифицирует конкретный тип события в рамках агрегата {@code UserProfile}.
     * Значение совпадает с именем константы перечисления в верхнем регистре.
     *
     * @see #USER_REGISTERED
     * @see #USER_PROFILE_UPDATED
     * @see #USER_VERIFIED
     */
    private final String eventType;

}
