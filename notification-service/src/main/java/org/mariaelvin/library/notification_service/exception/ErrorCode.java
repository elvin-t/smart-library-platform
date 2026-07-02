package org.mariaelvin.library.notification_service.exception;

public enum ErrorCode {

    INVALID_NOTIFICATION_REQUEST("NOTIFICATION_001", "Invalid notification request"),

    VALIDATION_ERROR("GEN_001", "Validation failed"),
    INTERNAL_SERVER_ERROR("GEN_999", "Internal server error"),
    DATA_INTEGRITY_ERROR("GEN_002", "Data integrity violation");

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