package org.mariaelvin.library.user_service.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.mariaelvin.library.user_service.dto.UpdateUserRequest;
import org.mariaelvin.library.user_service.dto.UserResponse;
import org.mariaelvin.library.user_service.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User APIs", description = "User profile and membership APIs")
public class UserController {

    private final UserService userService;

    @Operation(summary = "Get user by ID")
    @PreAuthorize("hasAuthority('USER_READ')")
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {

        UserResponse response = userService.getUserById(id);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get all users")
    @PreAuthorize("hasAuthority('USER_READ')")
    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {

        List<UserResponse> response = userService.getAllUsers();

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update user profile")
    @PreAuthorize("hasAuthority('USER_WRITE')")
    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request) {

        UserResponse response = userService.updateUser(id, request);

        return ResponseEntity.ok(response);
    }

 /*   @Operation(summary = "Update membership status")
    @PreAuthorize("hasAuthority('USER_WRITE')")
    @PatchMapping("/{id}/status")
    public ResponseEntity<UserResponse> updateMembershipStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateMembershipStatusRequest request) {

        UserResponse response = userService.updateMembershipStatus(id, request);

        return ResponseEntity.ok(response);
    }*/
}