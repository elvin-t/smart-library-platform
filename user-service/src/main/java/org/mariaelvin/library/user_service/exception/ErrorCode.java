package org.mariaelvin.library.user_service.exception;

public enum ErrorCode {

    // User Service Errors
    USER_NOT_FOUND("USER_001", "User not found"),
    USER_ALREADY_EXISTS("USER_002", "User already exists"),
    INVALID_USER_REQUEST("USER_003", "Invalid user request"),
    EMAIL_ALREADY_EXISTS("USER_004", "Email already exists"),

    // Membership Errors
    INVALID_MEMBERSHIP_STATUS("MEM_001", "Invalid membership status"),
    INVALID_MEMBERSHIP_TYPE("MEM_002", "Invalid membership type"),

    // Security Errors
    UNAUTHORIZED("SEC_001", "Authentication required"),
    ACCESS_DENIED("SEC_002", "Access denied"),

    // Validation Errors
    VALIDATION_ERROR("GEN_001", "Validation failed"),
    DATA_INTEGRITY_ERROR("GEN_002", "Data integrity violation"),

    // System Errors
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