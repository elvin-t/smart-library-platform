package org.mariaelvin.library.analytics_service.client;

import org.mariaelvin.library.analytics_service.dto.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient(
        name = "user-service",
        url = "${services.user-service.url}"
)
public interface UserServiceClient {

    @GetMapping("/api/users")
    List<UserResponse> getUsers();
}