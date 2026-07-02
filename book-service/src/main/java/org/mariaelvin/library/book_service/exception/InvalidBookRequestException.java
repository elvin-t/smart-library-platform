package org.mariaelvin.library.book_service.exception;

public class InvalidBookRequestException extends RuntimeException {

    public InvalidBookRequestException(String message) {
        super(message);
    }
}