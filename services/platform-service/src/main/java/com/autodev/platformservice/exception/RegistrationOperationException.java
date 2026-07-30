package com.autodev.platformservice.exception;

public class RegistrationOperationException extends RuntimeException {
    public RegistrationOperationException(String message) {
        super(message);
    }

    public RegistrationOperationException(String message, Object... args) {
        super(String.format(message, args));
    }
}
