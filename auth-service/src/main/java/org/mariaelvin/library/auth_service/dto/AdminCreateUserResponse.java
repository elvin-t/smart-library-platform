package org.mariaelvin.library.auth_service.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
public class AdminCreateUserResponse {

    private Long id;
    private String email;
    private String fullName;
    private String phone;
    private Set<String> roles;
    private boolean active;
    private boolean userProfileCreated;
}