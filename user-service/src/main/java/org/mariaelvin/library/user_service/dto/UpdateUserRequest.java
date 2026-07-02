package org.mariaelvin.library.user_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class UpdateUserRequest {

    @Schema(example = "MariaElvin")
    private String fullName;

    @Schema(example = "9876543210")
    private String phone;
}