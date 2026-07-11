package org.mariaelvin.library.analytics_service.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {

    ANALYTICS_001("ANALYTICS_001", "Invalid analytics request"),
    ANALYTICS_002("ANALYTICS_002", "Required user context is missing"),
    ANALYTICS_003("ANALYTICS_003", "Unable to fetch data from user service"),
    ANALYTICS_004("ANALYTICS_004", "Unable to fetch data from book service"),
    ANALYTICS_005("ANALYTICS_005", "Unable to fetch data from borrow service"),
    ANALYTICS_006("ANALYTICS_006", "Unable to fetch data from notification service"),
    ANALYTICS_007("ANALYTICS_007", "Dashboard summary generation failed"),
    ANALYTICS_500("ANALYTICS_500", "Internal analytics service error");

    private final String code;
    private final String message;

    ErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }
}