package org.mariaelvin.library.auth_service.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(UserNotFoundException ex) {

        log.warn("User not found: {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(ErrorCode.USER_NOT_FOUND, ex.getMessage()));
    }

    @ExceptionHandler({InvalidCredentialsException.class, BadCredentialsException.class})
    public ResponseEntity<ErrorResponse> handleInvalidCredentials(Exception ex) {

        log.warn("Invalid login attempt");

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse(ErrorCode.INVALID_CREDENTIALS, null));
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleUserAlreadyExists(UserAlreadyExistsException ex) {

        log.warn("Duplicate user: {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(ErrorCode.USER_ALREADY_EXISTS, ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {

        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(err -> err.getField() + " : " + err.getDefaultMessage())
                .orElse("Validation error");

        log.warn("Validation error: {}", message);

        return ResponseEntity.badRequest()
                .body(new ErrorResponse(ErrorCode.VALIDATION_ERROR, message));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {

        log.error("Unexpected error", ex);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(ErrorCode.INTERNAL_SERVER_ERROR, null));
    }

    @ExceptionHandler(InvalidUserRequestException.class)
    public ResponseEntity<ErrorResponse> handleInvalidUserRequest(InvalidUserRequestException ex) {

        log.warn("Invalid user request: {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(ErrorCode.INVALID_USER_REQUEST, ex.getMessage()));
    }


}