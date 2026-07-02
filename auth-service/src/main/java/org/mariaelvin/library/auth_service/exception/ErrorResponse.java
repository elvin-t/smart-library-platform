package org.mariaelvin.library.auth_service.exception;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ErrorResponse {

    private String code;
    private String message;
    private String path;
    private String traceId;
    private LocalDateTime timestamp;

    public static ErrorResponse of(ErrorCode errorCode,
                                   String message,
                                   String path,
                                   String traceId) {

        return ErrorResponse.builder()
                .code(errorCode.getCode())
                .message(message != null ? message : errorCode.getDefaultMessage())
                .path(path)
                .traceId(traceId)
                .timestamp(LocalDateTime.now())
                .build();
    }
}