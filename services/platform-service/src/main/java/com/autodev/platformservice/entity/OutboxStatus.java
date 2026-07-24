package com.autodev.platformservice.entity;


/** Статус отправки сообщения для Transactional Outbox */
public enum OutboxStatus {

    /** Ожидает отправки в Kafka */
    PENDING,
    /** Успешно отправлено */
    SENT,
    /** Ошибка при отправке (для ретраев или ручного разбора) */
    FAILED
}
