package org.mariaelvin.library.analytics_service.exception;

import lombok.Getter;

@Getter
public class AnalyticsServiceException extends RuntimeException {

    private final ErrorCode errorCode;

    public AnalyticsServiceException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public AnalyticsServiceException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public AnalyticsServiceException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
}