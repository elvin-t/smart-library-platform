package org.mariaelvin.library.auth_service.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
public class AdminAuthUserStatusResponse {

    private Long id;
    private String email;
    private Set<String> roles;
    private boolean active;
}