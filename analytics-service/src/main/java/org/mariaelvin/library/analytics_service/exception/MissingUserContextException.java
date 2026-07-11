package org.mariaelvin.library.analytics_service.exception;

public class MissingUserContextException extends AnalyticsServiceException {

    public MissingUserContextException(String message) {
        super(ErrorCode.ANALYTICS_002, message);
    }
}