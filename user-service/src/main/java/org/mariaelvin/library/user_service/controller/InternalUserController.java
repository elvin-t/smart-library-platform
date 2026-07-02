package org.mariaelvin.library.user_service.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.mariaelvin.library.user_service.dto.CreateUserRequest;
import org.mariaelvin.library.user_service.dto.UserResponse;
import org.mariaelvin.library.user_service.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/internal/users")
@RequiredArgsConstructor
@Tag(name = "Internal User APIs", description = "Internal APIs used by other microservices")
public class InternalUserController {

    private final UserService userService;

    @Operation(summary = "Create user profile from Auth Service")
    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {

        UserResponse response = userService.createUser(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}