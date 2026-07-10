package org.mariaelvin.library.notification_service.controller;

import lombok.RequiredArgsConstructor;
import org.mariaelvin.library.notification_service.dto.NotificationResponse;
import org.mariaelvin.library.notification_service.entity.NotificationStatus;
import org.mariaelvin.library.notification_service.entity.NotificationType;
import org.mariaelvin.library.notification_service.service.NotificationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<Page<NotificationResponse>> getAllNotifications(
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        return ResponseEntity.ok(notificationService.getAllNotifications(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<NotificationResponse> getNotificationById(@PathVariable Long id) {
        return ResponseEntity.ok(notificationService.getNotificationById(id));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<NotificationResponse>> getNotificationsByUserId(
            @PathVariable Long userId,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        return ResponseEntity.ok(notificationService.getNotificationsByUserId(userId, pageable));
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<Page<NotificationResponse>> getNotificationsByType(
            @PathVariable NotificationType type,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        return ResponseEntity.ok(notificationService.getNotificationsByType(type, pageable));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<Page<NotificationResponse>> getNotificationsByStatus(
            @PathVariable NotificationStatus status,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        return ResponseEntity.ok(notificationService.getNotificationsByStatus(status, pageable));
    }
}