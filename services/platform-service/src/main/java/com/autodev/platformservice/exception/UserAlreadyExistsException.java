package com.autodev.platformservice.exception;

public class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException(String message) {
        super(message);
    }

    public UserAlreadyExistsException(String message, Object... args) {
        super(String.format(message, args));
    }
}
