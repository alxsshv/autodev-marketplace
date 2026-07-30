package com.autodev.platformservice.entity;

/**
 * Константы событий, связанных с доменом User.
 * Используются для строгой типизации при работе с Outbox и Kafka.
 */
public final class UserEvents {
    private UserEvents() { }

    /** Топик Kafka, в который публикуются события пользователей */
    public static final String TOPIC = "platform-user-events";

    /** Тип агрегата (имя сущности) */
    public static final String AGGREGATE_TYPE = "User";

    /** Типы событий для Outbox*/
    public final class EventType {

        private EventType() { }

        /** Событие: Пользователь успешно зарегистрирован */
        public static final String USER_REGISTERED = "UserRegistered";

        /** Событие: Пользователь успешно обновлен */
        public static final String USER_UPDATED = "UserUpdated";

        /** Событие: Профиль пользователя подтвержден */
        public static final String USER_VERIFIED = "UserVerified";
    }
}
