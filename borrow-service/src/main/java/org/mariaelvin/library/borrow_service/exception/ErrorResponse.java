package org.mariaelvin.library.borrow_service.exception;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ErrorResponse {

    private String code;
    private String message;
    private String path;
    private LocalDateTime timestamp;

    public static ErrorResponse of(ErrorCode errorCode, String message, String path) {
        return ErrorResponse.builder()
                .code(errorCode.getCode())
                .message(message != null ? message : errorCode.getDefaultMessage())
                .path(path)
                .timestamp(LocalDateTime.now())
                .build();
    }
}