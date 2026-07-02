package org.mariaelvin.library.auth_service.exception;

import java.time.LocalDateTime;

public class ErrorResponse {

    private String code;
    private String message;
    private LocalDateTime timestamp;

    public ErrorResponse(ErrorCode errorCode, String message) {
        this.code = errorCode.getCode();
        this.message = message != null ? message : errorCode.getDefaultMessage();
        this.timestamp = LocalDateTime.now();
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}