package com.autodev.auth.dto.shared;

/** Перечисление, описывающее возможные инварианты событий,
 * поступающих, от Keycloak, при выполнении там операции с пользователем */
public enum Event {
    USER_CREATED,
    USER_UPDATED,
    USER_ENABLED,
    USER_DISABLED,
    USER_DELETED
}
