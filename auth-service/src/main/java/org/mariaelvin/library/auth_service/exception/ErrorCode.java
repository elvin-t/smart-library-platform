package org.mariaelvin.library.auth_service.exception;

public enum ErrorCode {

    // ✅ Auth Errors
    USER_NOT_FOUND("AUTH_001", "User not found"),
    INVALID_CREDENTIALS("AUTH_002", "Invalid credentials"),
    USER_ALREADY_EXISTS("AUTH_003", "User already exists"),
    TOKEN_EXPIRED("AUTH_004", "JWT token expired"),
    TOKEN_INVALID("AUTH_005", "Invalid JWT token"),

    // ✅ Validation Errors
    VALIDATION_ERROR("GEN_001", "Validation failed"),

    // ✅ System Errors
    INTERNAL_SERVER_ERROR("GEN_999", "Internal server error"),

    INVALID_USER_REQUEST("AUTH_006", "Invalid user request");

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