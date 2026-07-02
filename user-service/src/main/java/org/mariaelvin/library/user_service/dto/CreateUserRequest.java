package org.mariaelvin.library.user_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateUserRequest {

    @NotNull(message = "User ID is required")
    @Schema(example = "1")
    private Long id;

    @Email(message = "Invalid email format")
    @NotNull(message = "Email is required")
    @Schema(example = "admin@library.com")
    private String email;

    @Schema(example = "Admin User")
    private String fullName;

    @Schema(example = "9876543210")
    private String phone;
}