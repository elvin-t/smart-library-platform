package org.mariaelvin.library.book_service.exception;

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

    @ExceptionHandler(BookNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleBookNotFound(
            BookNotFoundException ex,
            HttpServletRequest request) {

        log.warn("Book not found: {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.of(
                        ErrorCode.BOOK_NOT_FOUND,
                        ex.getMessage(),
                        request.getRequestURI(),
                        MDC.get(TRACE_ID_KEY)
                ));
    }

    @ExceptionHandler(BookAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleBookAlreadyExists(
            BookAlreadyExistsException ex,
            HttpServletRequest request) {

        log.warn("Book already exists: {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErrorResponse.of(
                        ErrorCode.BOOK_ALREADY_EXISTS,
                        ex.getMessage(),
                        request.getRequestURI(),
                        MDC.get(TRACE_ID_KEY)
                ));
    }

    @ExceptionHandler(InvalidBookRequestException.class)
    public ResponseEntity<ErrorResponse> handleInvalidBookRequest(
            InvalidBookRequestException ex,
            HttpServletRequest request) {

        log.warn("Invalid book request: {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(
                        ErrorCode.INVALID_BOOK_REQUEST,
                        ex.getMessage(),
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

        log.warn("Invalid sort property: {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(
                        ErrorCode.INVALID_BOOK_REQUEST,
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
                        ErrorCode.INVALID_BOOK_REQUEST,
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
                        ErrorCode.INVALID_BOOK_REQUEST,
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
                        ErrorCode.BOOK_NOT_FOUND,
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