package org.mariaelvin.library.notification_service.service;

import lombok.RequiredArgsConstructor;
import org.mariaelvin.library.notification_service.dto.NotificationResponse;
import org.mariaelvin.library.notification_service.entity.Notification;
import org.mariaelvin.library.notification_service.entity.NotificationStatus;
import org.mariaelvin.library.notification_service.entity.NotificationType;
import org.mariaelvin.library.notification_service.repository.NotificationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class NotificationQueryService {

    private final NotificationRepository notificationRepository;

    @Transactional(readOnly = true)
    public Page<NotificationResponse> getAllNotifications(Pageable pageable) {
        return notificationRepository.findAll(pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public NotificationResponse getNotificationById(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found: " + id));

        return toResponse(notification);
    }

    @Transactional(readOnly = true)
    public Page<NotificationResponse> getNotificationsByUser(
            Long userId,
            Pageable pageable
    ) {
        return notificationRepository.findByUserId(userId, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<NotificationResponse> getNotificationsByType(
            NotificationType type,
            Pageable pageable
    ) {
        return notificationRepository.findByType(type, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<NotificationResponse> getNotificationsByStatus(
            NotificationStatus status,
            Pageable pageable
    ) {
        return notificationRepository.findByStatus(status, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Long getUnreadCountByUser(Long userId) {
        return notificationRepository.countByUserIdAndReadFalse(userId);
    }

    @Transactional
    public NotificationResponse markAsRead(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found: " + id));

        notification.setRead(true);
        notification.setReadAt(LocalDateTime.now());

        return toResponse(notificationRepository.save(notification));
    }

    @Transactional
    public void markAllAsReadByUser(Long userId) {
        notificationRepository.markAllAsReadByUserId(
                userId,
                LocalDateTime.now()
        );
    }

    private NotificationResponse toResponse(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getUserId(),
                notification.getEmail(),
                notification.getType(),
                notification.getChannel(),
                notification.getStatus(),
                notification.getSubject(),
                notification.getMessage(),
                notification.getBookId(),
                notification.getBorrowRecordId(),
                notification.isRead(),
                notification.getReadAt(),
                notification.getCreatedAt()
        );
    }
}