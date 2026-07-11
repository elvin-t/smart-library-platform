package org.mariaelvin.library.auth_service.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.mariaelvin.library.auth_service.dto.*;
import org.mariaelvin.library.auth_service.service.AuthService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor   // ✅ Lombok injection
@Tag(name = "Auth APIs", description = "Authentication & Authorization APIs (JWT + RBAC)")
public class AuthController {

    private final AuthService authService;


    // ✅ ADMIN CREATE USER API - MEMBER / LIBRARIAN
    @Operation(
            summary = "Admin create user",
            description = "Admin creates MEMBER or LIBRARIAN user with login access"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User created successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "409", description = "User already exists")
    })
    @PreAuthorize("hasAuthority('USER_WRITE')")
    @PostMapping("/admin/users")
    public ResponseEntity<AdminCreateUserResponse> createUserByAdmin(
            @Valid @RequestBody AdminCreateUserRequest request) {

        AdminCreateUserResponse response = authService.createUserByAdmin(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


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

    @Operation(
            summary = "Deactivate user login",
            description = "Admin disables login access for a user"
    )
    @PreAuthorize("hasAuthority('USER_WRITE')")
    @PatchMapping("/admin/users/{userId}/deactivate")
    public ResponseEntity<AdminUserStatusResponse> deactivateUser(
            @PathVariable Long userId) {

        return ResponseEntity.ok(authService.deactivateUser(userId));
    }

    @Operation(
            summary = "Activate user login",
            description = "Admin enables login access for a user"
    )
    @PreAuthorize("hasAuthority('USER_WRITE')")
    @PatchMapping("/admin/users/{userId}/activate")
    public ResponseEntity<AdminUserStatusResponse> activateUser(
            @PathVariable Long userId) {

        return ResponseEntity.ok(authService.activateUser(userId));
    }

    @Operation(
    description = "Admin gets login active/inactive status for a user"
            )
    @PreAuthorize("hasAuthority('USER_READ')")
    @GetMapping("/admin/users/{userId}/status")
    public ResponseEntity<AdminAuthUserStatusResponse> getAuthUserStatus(
            @PathVariable Long userId) {

        return ResponseEntity.ok(authService.getAuthUserStatus(userId));
    }

    @Operation(
            summary = "Get all auth user login statuses",
            description = "Admin gets login active/inactive status for all users"
    )
    @PreAuthorize("hasAuthority('USER_READ')")
    @GetMapping("/admin/users/statuses")
    public ResponseEntity<List<AdminAuthUserStatusResponse>> getAllAuthUserStatuses() {

        return ResponseEntity.ok(authService.getAllAuthUserStatuses());
    }



}