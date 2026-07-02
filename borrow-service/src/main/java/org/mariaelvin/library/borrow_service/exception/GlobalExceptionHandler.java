package org.mariaelvin.library.borrow_service.exception;

import feign.FeignException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BorrowRecordNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleBorrowRecordNotFound(
            BorrowRecordNotFoundException ex,
            HttpServletRequest request) {

        log.warn("Borrow record not found: {}", ex.getMessage());

        ErrorResponse response = ErrorResponse.of(
                ErrorCode.BORROW_RECORD_NOT_FOUND,
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(InvalidBorrowRequestException.class)
    public ResponseEntity<ErrorResponse> handleInvalidBorrowRequest(
            InvalidBorrowRequestException ex,
            HttpServletRequest request) {

        log.warn("Invalid borrow request: {}", ex.getMessage());

        ErrorResponse response = ErrorResponse.of(
                ErrorCode.INVALID_BORROW_REQUEST,
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(BookServiceException.class)
    public ResponseEntity<ErrorResponse> handleBookServiceException(
            BookServiceException ex,
            HttpServletRequest request) {

        log.warn("Book service error: {}", ex.getMessage());

        ErrorResponse response = ErrorResponse.of(
                ErrorCode.BOOK_SERVICE_ERROR,
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(response);
    }

    @ExceptionHandler(FeignException.class)
    public ResponseEntity<ErrorResponse> handleFeignException(
            FeignException ex,
            HttpServletRequest request) {

        log.warn("Feign client error while calling external service. status={}, message={}",
                ex.status(), ex.getMessage());

        ErrorResponse response = ErrorResponse.of(
                ErrorCode.BOOK_SERVICE_ERROR,
                "Unable to communicate with Book Service",
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(response);
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
                ErrorCode.INVALID_BORROW_REQUEST,
                "API resource not found",
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
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