package org.mariaelvin.library.analytics_service.client;

import org.mariaelvin.library.analytics_service.dto.NotificationResponse;
import org.mariaelvin.library.analytics_service.dto.PageResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(
        name = "notification-service",
        url = "${services.notification-service.url}"
)
public interface NotificationServiceClient {

    @GetMapping("/api/notifications")
    PageResponse<NotificationResponse> getAllNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "1") int size
    );

    @GetMapping("/api/notifications/user/{userId}")
    PageResponse<NotificationResponse> getNotificationsByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "1") int size
    );
}