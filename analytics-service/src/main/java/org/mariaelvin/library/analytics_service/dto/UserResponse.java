package org.mariaelvin.library.analytics_service.dto;

import lombok.Data;

@Data
public class UserResponse {

    private Long id;
    private String email;
    private String fullName;
    private String phone;
    private String membershipType;
    private String membershipStatus;
}