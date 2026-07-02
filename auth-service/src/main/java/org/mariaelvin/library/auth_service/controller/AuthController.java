package org.mariaelvin.library.auth_service.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.mariaelvin.library.auth_service.dto.AuthResponse;
import org.mariaelvin.library.auth_service.dto.LoginRequest;
import org.mariaelvin.library.auth_service.dto.RegisterRequest;
import org.mariaelvin.library.auth_service.service.AuthService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor   // ✅ Lombok injection
@Tag(name = "Auth APIs", description = "Authentication & Authorization APIs (JWT + RBAC)")
public class AuthController {

    private final AuthService authService;

    // ✅ REGISTER API
    @Operation(
            summary = "Register new user",
            description = "Creates a new user with default MEMBER role"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User registered successfully"),
            @ApiResponse(responseCode = "409", description = "User already exists",
                    content = @Content(schema = @Schema(example = "{ \"code\": \"AUTH_003\", \"message\": \"User already exists\" }"))),
            @ApiResponse(responseCode = "400", description = "Validation error")
    })
    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequest request) {

        authService.register(request);

        return ResponseEntity.ok("User registered successfully");
    }

    // ✅ LOGIN API
    @Operation(
            summary = "Authenticate user",
            description = "Validates credentials and returns JWT token with roles and permissions"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "401", description = "Invalid credentials",
                    content = @Content(schema = @Schema(example = "{ \"code\": \"AUTH_002\", \"message\": \"Invalid credentials\" }")))
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {

        String token = authService.login(request);

        return ResponseEntity.ok(new AuthResponse(token));
    }

}