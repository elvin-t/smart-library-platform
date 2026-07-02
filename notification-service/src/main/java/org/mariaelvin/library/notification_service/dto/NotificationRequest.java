package org.mariaelvin.library.notification_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.mariaelvin.library.notification_service.entity.NotificationChannel;
import org.mariaelvin.library.notification_service.entity.NotificationType;

@Data
public class NotificationRequest {

    @NotNull(message = "User ID is required")
    private Long userId;

    private String email;

    @NotNull(message = "Notification type is required")
    private NotificationType type;

    @NotNull(message = "Notification channel is required")
    private NotificationChannel channel;

    @NotBlank(message = "Subject is required")
    private String subject;

    @NotBlank(message = "Message is required")
    private String message;

    private Long bookId;

    private Long borrowRecordId;
}