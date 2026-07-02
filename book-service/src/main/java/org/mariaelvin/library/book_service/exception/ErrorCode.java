package org.mariaelvin.library.book_service.exception;

public enum ErrorCode {

    BOOK_NOT_FOUND("BOOK_001", "Book not found"),
    BOOK_ALREADY_EXISTS("BOOK_002", "Book already exists"),
    INVALID_BOOK_REQUEST("BOOK_003", "Invalid book request"),

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