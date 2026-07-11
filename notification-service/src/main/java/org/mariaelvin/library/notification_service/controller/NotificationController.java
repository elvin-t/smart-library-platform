package org.mariaelvin.library.notification_service.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.mariaelvin.library.notification_service.dto.NotificationResponse;
import org.mariaelvin.library.notification_service.entity.NotificationStatus;
import org.mariaelvin.library.notification_service.entity.NotificationType;
import org.mariaelvin.library.notification_service.service.NotificationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notification APIs", description = "Notification read APIs used by frontend")
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(summary = "Get all notifications")
    @GetMapping
    public ResponseEntity<Page<NotificationResponse>> getAllNotifications(
            @PageableDefault(
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            ) Pageable pageable
    ) {
        return ResponseEntity.ok(notificationService.getAllNotifications(pageable));
    }

    @Operation(summary = "Get notification by id")
    @GetMapping("/{id}")
    public ResponseEntity<NotificationResponse> getNotificationById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(notificationService.getNotificationById(id));
    }

    @Operation(summary = "Get notifications by user id")
    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<NotificationResponse>> getNotificationsByUserId(
            @PathVariable Long userId,
            @PageableDefault(
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            ) Pageable pageable
    ) {
        return ResponseEntity.ok(
                notificationService.getNotificationsByUserId(userId, pageable)
        );
    }

    @Operation(summary = "Get notifications by type")
    @GetMapping("/type/{type}")
    public ResponseEntity<Page<NotificationResponse>> getNotificationsByType(
            @PathVariable NotificationType type,
            @PageableDefault(
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            ) Pageable pageable
    ) {
        return ResponseEntity.ok(
                notificationService.getNotificationsByType(type, pageable)
        );
    }

    @Operation(summary = "Get notifications by status")
    @GetMapping("/status/{status}")
    public ResponseEntity<Page<NotificationResponse>> getNotificationsByStatus(
            @PathVariable NotificationStatus status,
            @PageableDefault(
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            ) Pageable pageable
    ) {
        return ResponseEntity.ok(
                notificationService.getNotificationsByStatus(status, pageable)
        );
    }

    @Operation(summary = "Get unread notification count by user id")
    @GetMapping("/user/{userId}/unread-count")
    public ResponseEntity<Long> getUnreadCountByUserId(
            @PathVariable Long userId
    ) {
        return ResponseEntity.ok(
                notificationService.getUnreadCountByUserId(userId)
        );
    }

    @Operation(summary = "Mark notification as read")
    @PatchMapping("/{id}/read")
    public ResponseEntity<NotificationResponse> markAsRead(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(notificationService.markAsRead(id));
    }

    @Operation(summary = "Mark all notifications as read by user id")
    @PatchMapping("/user/{userId}/read-all")
    public ResponseEntity<Void> markAllAsReadByUserId(
            @PathVariable Long userId
    ) {
        notificationService.markAllAsReadByUserId(userId);

        return ResponseEntity.noContent().build();
    }
}