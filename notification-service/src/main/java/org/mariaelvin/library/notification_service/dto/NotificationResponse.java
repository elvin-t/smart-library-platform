package org.mariaelvin.library.notification_service.dto;

import lombok.Builder;
import lombok.Data;
import org.mariaelvin.library.notification_service.entity.NotificationChannel;
import org.mariaelvin.library.notification_service.entity.NotificationStatus;
import org.mariaelvin.library.notification_service.entity.NotificationType;

import java.time.LocalDateTime;

@Data
@Builder
public class NotificationResponse {

    private Long id;
    private Long userId;
    private String email;
    private NotificationType type;
    private NotificationChannel channel;
    private NotificationStatus status;
    private String subject;
    private String message;
    private Long bookId;
    private Long borrowRecordId;
    private LocalDateTime createdAt;
    private LocalDateTime sentAt;
}