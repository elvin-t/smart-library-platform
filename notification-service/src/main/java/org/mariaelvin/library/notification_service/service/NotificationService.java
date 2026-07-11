package org.mariaelvin.library.notification_service.service;

import lombok.RequiredArgsConstructor;
import org.mariaelvin.library.notification_service.dto.NotificationRequest;
import org.mariaelvin.library.notification_service.dto.NotificationResponse;
import org.mariaelvin.library.notification_service.entity.Notification;
import org.mariaelvin.library.notification_service.entity.NotificationStatus;
import org.mariaelvin.library.notification_service.entity.NotificationType;
import org.mariaelvin.library.notification_service.exception.NotificationNotFoundException;
import org.mariaelvin.library.notification_service.repository.NotificationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    /**
     * Existing internal notification creation method.
     */
    @Transactional
    public NotificationResponse sendNotification(NotificationRequest request) {

        Notification notification = Notification.builder()
                .userId(request.getUserId())
                .email(request.getEmail())
                .type(request.getType())
                .channel(request.getChannel())
                .status(NotificationStatus.SENT)
                .subject(request.getSubject())
                .message(request.getMessage())
                .bookId(request.getBookId())
                .borrowRecordId(request.getBorrowRecordId())
                .sentAt(LocalDateTime.now())
                .read(false)
                .build();

        Notification savedNotification = notificationRepository.save(notification);

        return toResponse(savedNotification);
    }

    @Transactional(readOnly = true)
    public Page<NotificationResponse> getAllNotifications(Pageable pageable) {
        return notificationRepository.findAll(pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public NotificationResponse getNotificationById(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new NotificationNotFoundException(
                        "Notification not found with id: " + id
                ));

        return toResponse(notification);
    }

    @Transactional(readOnly = true)
    public Page<NotificationResponse> getNotificationsByUserId(
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
    public long getUnreadCountByUserId(Long userId) {
        return notificationRepository.countByUserIdAndReadFalse(userId);
    }

    @Transactional
    public NotificationResponse markAsRead(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new NotificationNotFoundException(
                        "Notification not found with id: " + id
                ));

        if (!notification.isRead()) {
            notification.setRead(true);
            notification.setReadAt(LocalDateTime.now());
        }

        Notification savedNotification = notificationRepository.save(notification);

        return toResponse(savedNotification);
    }

    @Transactional
    public void markAllAsReadByUserId(Long userId) {
        List<Notification> unreadNotifications =
                notificationRepository.findByUserIdAndReadFalse(userId);

        LocalDateTime now = LocalDateTime.now();

        unreadNotifications.forEach(notification -> {
            notification.setRead(true);
            notification.setReadAt(now);
        });

        notificationRepository.saveAll(unreadNotifications);
    }

    private NotificationResponse toResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .userId(notification.getUserId())
                .email(notification.getEmail())
                .type(notification.getType())
                .channel(notification.getChannel())
                .status(notification.getStatus())
                .subject(notification.getSubject())
                .message(notification.getMessage())
                .bookId(notification.getBookId())
                .borrowRecordId(notification.getBorrowRecordId())
                .read(notification.isRead())
                .readAt(notification.getReadAt())
                .createdAt(notification.getCreatedAt())
                .sentAt(notification.getSentAt())
                .build();
    }
}