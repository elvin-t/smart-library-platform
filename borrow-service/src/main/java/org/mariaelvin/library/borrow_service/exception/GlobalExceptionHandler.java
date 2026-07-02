package org.mariaelvin.library.borrow_service.exception;

import feign.FeignException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    private static final String TRACE_ID_KEY = "traceId";

    @ExceptionHandler(BorrowRecordNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleBorrowRecordNotFound(
            BorrowRecordNotFoundException ex,
            HttpServletRequest request) {

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.of(
                        ErrorCode.BORROW_RECORD_NOT_FOUND,
                        ex.getMessage(),
                        request.getRequestURI(),
                        MDC.get(TRACE_ID_KEY)
                ));
    }

    @ExceptionHandler(InvalidBorrowRequestException.class)
    public ResponseEntity<ErrorResponse> handleInvalidBorrowRequest(
            InvalidBorrowRequestException ex,
            HttpServletRequest request) {

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(
                        ErrorCode.INVALID_BORROW_REQUEST,
                        ex.getMessage(),
                        request.getRequestURI(),
                        MDC.get(TRACE_ID_KEY)
                ));
    }

    @ExceptionHandler(BookServiceException.class)
    public ResponseEntity<ErrorResponse> handleBookServiceException(
            BookServiceException ex,
            HttpServletRequest request) {

        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(ErrorResponse.of(
                        ErrorCode.BOOK_SERVICE_ERROR,
                        ex.getMessage(),
                        request.getRequestURI(),
                        MDC.get(TRACE_ID_KEY)
                ));
    }

    @ExceptionHandler(FeignException.class)
    public ResponseEntity<ErrorResponse> handleFeignException(
            FeignException ex,
            HttpServletRequest request) {

        log.warn("Feign client error. status={}, message={}", ex.status(), ex.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(ErrorResponse.of(
                        ErrorCode.BOOK_SERVICE_ERROR,
                        "Unable to communicate with external service",
                        request.getRequestURI(),
                        MDC.get(TRACE_ID_KEY)
                ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(error -> error.getField() + " : " + error.getDefaultMessage())
                .orElse("Validation failed");

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(
                        ErrorCode.VALIDATION_ERROR,
                        message,
                        request.getRequestURI(),
                        MDC.get(TRACE_ID_KEY)
                ));
    }

    @ExceptionHandler(PropertyReferenceException.class)
    public ResponseEntity<ErrorResponse> handleInvalidSortProperty(
            PropertyReferenceException ex,
            HttpServletRequest request) {

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(
                        ErrorCode.INVALID_BORROW_REQUEST,
                        "Invalid sort field. Please use a valid field.",
                        request.getRequestURI(),
                        MDC.get(TRACE_ID_KEY)
                ));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(
            DataIntegrityViolationException ex,
            HttpServletRequest request) {

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErrorResponse.of(
                        ErrorCode.DATA_INTEGRITY_ERROR,
                        "Duplicate or invalid database value",
                        request.getRequestURI(),
                        MDC.get(TRACE_ID_KEY)
                ));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(
            AccessDeniedException ex,
            HttpServletRequest request) {

        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ErrorResponse.of(
                        ErrorCode.ACCESS_DENIED,
                        "You do not have permission to access this resource",
                        request.getRequestURI(),
                        MDC.get(TRACE_ID_KEY)
                ));
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleUnsupportedMediaType(
            HttpMediaTypeNotSupportedException ex,
            HttpServletRequest request) {

        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                .body(ErrorResponse.of(
                        ErrorCode.INVALID_BORROW_REQUEST,
                        "Unsupported content type. Please use application/json",
                        request.getRequestURI(),
                        MDC.get(TRACE_ID_KEY)
                ));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException ex,
            HttpServletRequest request) {

        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(ErrorResponse.of(
                        ErrorCode.INVALID_BORROW_REQUEST,
                        "HTTP method not supported. Please check the API method.",
                        request.getRequestURI(),
                        MDC.get(TRACE_ID_KEY)
                ));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResourceFound(
            NoResourceFoundException ex,
            HttpServletRequest request) {

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.of(
                        ErrorCode.BORROW_RECORD_NOT_FOUND,
                        "API resource not found",
                        request.getRequestURI(),
                        MDC.get(TRACE_ID_KEY)
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(
            Exception ex,
            HttpServletRequest request) {

        log.error("Unexpected error occurred", ex);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.of(
                        ErrorCode.INTERNAL_SERVER_ERROR,
                        "Something went wrong. Please try again later.",
                        request.getRequestURI(),
                        MDC.get(TRACE_ID_KEY)
                ));
    }
}