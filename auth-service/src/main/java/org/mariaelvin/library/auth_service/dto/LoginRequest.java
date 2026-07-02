package org.mariaelvin.library.auth_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class LoginRequest {

    @Schema(example = "user@gmail.com")
    private String email;

    @Schema(example = "password123")
    private String password;
}