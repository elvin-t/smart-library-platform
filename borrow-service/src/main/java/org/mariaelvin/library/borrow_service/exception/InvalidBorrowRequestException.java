package org.mariaelvin.library.borrow_service.exception;

public class InvalidBorrowRequestException extends RuntimeException {

    public InvalidBorrowRequestException(String message) {
        super(message);
    }
}