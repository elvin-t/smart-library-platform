package org.mariaelvin.library.notification_service.controller;

import lombok.RequiredArgsConstructor;
import org.mariaelvin.library.notification_service.dto.NotificationResponse;
import org.mariaelvin.library.notification_service.entity.NotificationStatus;
import org.mariaelvin.library.notification_service.entity.NotificationType;
import org.mariaelvin.library.notification_service.service.NotificationQueryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationQueryService notificationQueryService;

    @GetMapping
    public ResponseEntity<Page<NotificationResponse>> getAllNotifications(Pageable pageable) {
        return ResponseEntity.ok(
                notificationQueryService.getAllNotifications(pageable)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<NotificationResponse> getNotificationById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                notificationQueryService.getNotificationById(id)
        );
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<NotificationResponse>> getNotificationsByUser(
            @PathVariable Long userId,
            Pageable pageable
    ) {
        return ResponseEntity.ok(
                notificationQueryService.getNotificationsByUser(userId, pageable)
        );
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<Page<NotificationResponse>> getNotificationsByType(
            @PathVariable NotificationType type,
            Pageable pageable
    ) {
        return ResponseEntity.ok(
                notificationQueryService.getNotificationsByType(type, pageable)
        );
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<Page<NotificationResponse>> getNotificationsByStatus(
            @PathVariable NotificationStatus status,
            Pageable pageable
    ) {
        return ResponseEntity.ok(
                notificationQueryService.getNotificationsByStatus(status, pageable)
        );
    }

    @GetMapping("/user/{userId}/unread-count")
    public ResponseEntity<Long> getUnreadCountByUser(
            @PathVariable Long userId
    ) {
        return ResponseEntity.ok(
                notificationQueryService.getUnreadCountByUser(userId)
        );
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<NotificationResponse> markAsRead(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                notificationQueryService.markAsRead(id)
        );
    }

    @PatchMapping("/user/{userId}/read-all")
    public ResponseEntity<Void> markAllAsReadByUser(
            @PathVariable Long userId
    ) {
        notificationQueryService.markAllAsReadByUser(userId);
        return ResponseEntity.noContent().build();
    }
}