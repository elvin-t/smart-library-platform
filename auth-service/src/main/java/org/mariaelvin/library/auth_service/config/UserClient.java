package org.mariaelvin.library.auth_service.config;

import org.mariaelvin.library.auth_service.dto.CreateUserRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "user-service", url = "${services.user-service.url}")
public interface UserClient {

    @PostMapping("/api/internal/users")
    void createUser(@RequestBody CreateUserRequest request);
}