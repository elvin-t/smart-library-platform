package org.mariaelvin.library.borrow_service.client;

import org.mariaelvin.library.borrow_service.dto.NotificationRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(
        name = "notification-service",
        url = "${services.notification-service.url}"
)
public interface NotificationClient {

    @PostMapping("/api/internal/notifications")
    void sendNotification(
            @RequestHeader("X-Internal-Token") String internalToken,
            @RequestBody NotificationRequest request
    );
}