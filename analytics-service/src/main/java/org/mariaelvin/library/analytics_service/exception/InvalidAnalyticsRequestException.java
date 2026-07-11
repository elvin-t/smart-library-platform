package org.mariaelvin.library.analytics_service.exception;

public class InvalidAnalyticsRequestException extends AnalyticsServiceException {

    public InvalidAnalyticsRequestException(String message) {
        super(ErrorCode.ANALYTICS_001, message);
    }
}