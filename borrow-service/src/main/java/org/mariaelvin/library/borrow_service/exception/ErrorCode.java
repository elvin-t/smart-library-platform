package org.mariaelvin.library.borrow_service.exception;

public enum ErrorCode {

    BORROW_RECORD_NOT_FOUND("BORROW_001", "Borrow record not found"),
    INVALID_BORROW_REQUEST("BORROW_002", "Invalid borrow request"),
    BOOK_SERVICE_ERROR("BORROW_003", "Book service communication failed"),

    UNAUTHORIZED("SEC_001", "Authentication required"),
    ACCESS_DENIED("SEC_002", "Access denied"),

    VALIDATION_ERROR("GEN_001", "Validation failed"),
    DATA_INTEGRITY_ERROR("GEN_002", "Data integrity violation"),
    INTERNAL_SERVER_ERROR("GEN_999", "Internal server error");

    private final String code;
    private final String defaultMessage;

    ErrorCode(String code, String defaultMessage) {
        this.code = code;
        this.defaultMessage = defaultMessage;
    }

    public String getCode() {
        return code;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }
}