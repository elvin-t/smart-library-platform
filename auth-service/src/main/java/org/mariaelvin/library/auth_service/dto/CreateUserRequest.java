package org.mariaelvin.library.auth_service.dto;

import lombok.Data;

@Data
public class CreateUserRequest {

    private Long id;
    private String email;
    private String fullName;
    private String phone;
}