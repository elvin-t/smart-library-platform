package org.mariaelvin.library.auth_service.exception;

public class InvalidUserRequestException extends RuntimeException {

    public InvalidUserRequestException(String message) {
        super(message);
    }
}