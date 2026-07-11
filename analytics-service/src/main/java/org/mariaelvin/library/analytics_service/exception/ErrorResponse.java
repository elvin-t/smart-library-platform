package org.mariaelvin.library.analytics_service.exception;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ErrorResponse {

    private String code;
    private String message;
    private String path;
    private String traceId;
    private LocalDateTime timestamp;
}