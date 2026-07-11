package org.mariaelvin.library.analytics_service.exception;

public class DashboardSummaryException extends AnalyticsServiceException {

    public DashboardSummaryException(String message, Throwable cause) {
        super(ErrorCode.ANALYTICS_007, message, cause);
    }

    public DashboardSummaryException(String message) {
        super(ErrorCode.ANALYTICS_007, message);
    }
}