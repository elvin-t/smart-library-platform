package org.mariaelvin.library.borrow_service.exception;

public class BorrowRecordNotFoundException extends RuntimeException {

    public BorrowRecordNotFoundException(String message) {
        super(message);
    }
}