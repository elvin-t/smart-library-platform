package org.mariaelvin.library.analytics_service.exception;

public class ExternalServiceException extends AnalyticsServiceException {

    public ExternalServiceException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }

    public ExternalServiceException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}