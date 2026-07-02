package org.mariaelvin.library.book_service.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BookNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleBookNotFound(
            BookNotFoundException ex,
            HttpServletRequest request) {

        log.warn("Book not found: {}", ex.getMessage());

        ErrorResponse response = ErrorResponse.of(
                ErrorCode.BOOK_NOT_FOUND,
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(BookAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleBookAlreadyExists(
            BookAlreadyExistsException ex,
            HttpServletRequest request) {

        log.warn("Book already exists: {}", ex.getMessage());

        ErrorResponse response = ErrorResponse.of(
                ErrorCode.BOOK_ALREADY_EXISTS,
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(InvalidBookRequestException.class)
    public ResponseEntity<ErrorResponse> handleInvalidBookRequest(
            InvalidBookRequestException ex,
            HttpServletRequest request) {

        log.warn("Invalid book request: {}", ex.getMessage());

        ErrorResponse response = ErrorResponse.of(
                ErrorCode.INVALID_BOOK_REQUEST,
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        String errorMessage = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(error -> error.getField() + " : " + error.getDefaultMessage())
                .orElse("Validation failed");

        log.warn("Validation failed: {}", errorMessage);

        ErrorResponse response = ErrorResponse.of(
                ErrorCode.VALIDATION_ERROR,
                errorMessage,
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(
            DataIntegrityViolationException ex,
            HttpServletRequest request) {

        log.warn("Data integrity violation: {}", ex.getMessage());

        ErrorResponse response = ErrorResponse.of(
                ErrorCode.DATA_INTEGRITY_ERROR,
                "Duplicate or invalid database value",
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(
            AccessDeniedException ex,
            HttpServletRequest request) {

        log.warn("Access denied: {}", ex.getMessage());

        ErrorResponse response = ErrorResponse.of(
                ErrorCode.ACCESS_DENIED,
                "You do not have permission to access this resource",
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResourceFound(
            NoResourceFoundException ex,
            HttpServletRequest request) {

        log.warn("Resource not found: {}", request.getRequestURI());

        ErrorResponse response = ErrorResponse.of(
                ErrorCode.BOOK_NOT_FOUND,
                "API resource not found",
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException ex,
            HttpServletRequest request) {

        log.warn("HTTP method not supported: {}", ex.getMessage());

        ErrorResponse response = ErrorResponse.of(
                ErrorCode.INVALID_BOOK_REQUEST,
                "HTTP method not supported. Please check the API method.",
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex,
            HttpServletRequest request) {

        log.error("Unexpected error occurred", ex);

        ErrorResponse response = ErrorResponse.of(
                ErrorCode.INTERNAL_SERVER_ERROR,
                "Something went wrong. Please try again later.",
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}