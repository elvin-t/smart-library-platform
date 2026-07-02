package org.mariaelvin.library.notification_service.exception;

public class InvalidNotificationRequestException extends RuntimeException {

    public InvalidNotificationRequestException(String message) {
        super(message);
    }
}