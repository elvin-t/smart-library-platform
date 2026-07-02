package org.mariaelvin.library.user_service.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserResponse {

    @Schema(example = "1")
    private Long id;

    @Schema(example = "admin@library.com")
    private String email;

    @Schema(example = "Admin User")
    private String fullName;

    @Schema(example = "9876543210")
    private String phone;

    @Schema(example = "STANDARD")
    private MembershipType membershipType;

    @Schema(example = "ACTIVE")
    private MembershipStatus membershipStatus;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}