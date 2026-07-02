package org.mariaelvin.library.book_service.exception;

public class BookAlreadyExistsException extends RuntimeException {

    public BookAlreadyExistsException(String message) {
        super(message);
    }
}