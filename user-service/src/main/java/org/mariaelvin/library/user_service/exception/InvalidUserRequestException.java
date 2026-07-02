package org.mariaelvin.library.user_service.exception;

public class InvalidUserRequestException extends RuntimeException {

    public InvalidUserRequestException(String message) {
        super(message);
    }
}