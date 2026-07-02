package org.mariaelvin.library.auth_service.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mariaelvin.library.auth_service.client.UserClient;
import org.mariaelvin.library.auth_service.dto.CreateUserRequest;
import org.mariaelvin.library.auth_service.exception.UserServiceException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceClientFacade {

    private final UserClient userClient;

    @Value("${app.internal.token}")
    private String internalToken;

    @Retry(name = "userServiceCreateRetry", fallbackMethod = "createUserFallback")
    @CircuitBreaker(name = "userService", fallbackMethod = "createUserFallback")
    public void createUser(CreateUserRequest request) {
        userClient.createUser(internalToken, request);
    }

    public void createUserFallback(CreateUserRequest request, Throwable ex) {

        log.error(
                "User Service createUser failed. userId={}, email={}, reason={}",
                request.getId(),
                request.getEmail(),
                ex.getMessage(),
                ex
        );

        throw new UserServiceException(
                "Unable to create user profile in User Service. Please try again later.",
                ex
        );
    }
}