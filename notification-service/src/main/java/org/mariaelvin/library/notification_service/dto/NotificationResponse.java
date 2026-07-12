package org.mariaelvin.library.notification_service.dto;

import org.mariaelvin.library.notification_service.entity.NotificationChannel;
import org.mariaelvin.library.notification_service.entity.NotificationStatus;
import org.mariaelvin.library.notification_service.entity.NotificationType;

import java.time.LocalDateTime;

public record NotificationResponse(
        Long id,
        Long userId,
        String email,
        NotificationType type,
        NotificationChannel channel,
        NotificationStatus status,
        String subject,
        String message,
        Long bookId,
        Long borrowRecordId,
        Boolean read,
        LocalDateTime readAt,
        LocalDateTime createdAt
) {
}