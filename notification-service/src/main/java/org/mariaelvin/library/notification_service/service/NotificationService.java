package org.mariaelvin.library.notification_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mariaelvin.library.notification_service.dto.NotificationRequest;
import org.mariaelvin.library.notification_service.dto.NotificationResponse;
import org.mariaelvin.library.notification_service.entity.Notification;
import org.mariaelvin.library.notification_service.entity.NotificationStatus;
import org.mariaelvin.library.notification_service.entity.NotificationType;
import org.mariaelvin.library.notification_service.exception.InvalidNotificationRequestException;
import org.mariaelvin.library.notification_service.repository.NotificationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;

    /**
     * Send notification using REST request.
     *
     * Current implementation:
     * - Saves notification as PENDING
     * - Logs notification message
     * - Marks notification as SENT
     *
     * Later this can be replaced with:
     * - Email provider
     * - SMS provider
     * - Kafka consumer
     */
    @Transactional
    public NotificationResponse sendNotification(NotificationRequest request) {

        validateRequest(request);

        Notification notification = Notification.builder()
                .userId(request.getUserId())
                .email(request.getEmail())
                .type(request.getType())
                .channel(request.getChannel())
                .status(NotificationStatus.PENDING)
                .subject(request.getSubject())
                .message(request.getMessage())
                .bookId(request.getBookId())
                .borrowRecordId(request.getBorrowRecordId())
                .build();

        Notification savedNotification = notificationRepository.save(notification);

        try {
            log.info(
                    "Sending notification. type={}, channel={}, userId={}, email={}, subject={}, message={}",
                    savedNotification.getType(),
                    savedNotification.getChannel(),
                    savedNotification.getUserId(),
                    savedNotification.getEmail(),
                    savedNotification.getSubject(),
                    savedNotification.getMessage()
            );

            /*
             * Simulated notification sending.
             * For now, we are only logging.
             * Later, integrate JavaMailSender / AWS SES / SMS provider.
             */
            savedNotification.markSent();

            Notification sentNotification = notificationRepository.save(savedNotification);

            log.info(
                    "Notification sent successfully. notificationId={}, type={}, userId={}",
                    sentNotification.getId(),
                    sentNotification.getType(),
                    sentNotification.getUserId()
            );

            return toResponse(sentNotification);

        } catch (Exception ex) {

            log.error(
                    "Failed to send notification. notificationId={}, userId={}, reason={}",
                    savedNotification.getId(),
                    savedNotification.getUserId(),
                    ex.getMessage(),
                    ex
            );

            savedNotification.markFailed();

            Notification failedNotification = notificationRepository.save(savedNotification);

            return toResponse(failedNotification);
        }
    }

    /**
     * Get notification by ID.
     */
    @Transactional(readOnly = true)
    public NotificationResponse getNotificationById(Long id) {

        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() ->
                        new InvalidNotificationRequestException("Notification not found with id: " + id));

        return toResponse(notification);
    }

    /**
     * Get notifications by user ID.
     */
    @Transactional(readOnly = true)
    public Page<NotificationResponse> getNotificationsByUserId(Long userId, Pageable pageable) {

        if (userId == null) {
            throw new InvalidNotificationRequestException("User ID is required");
        }

        return notificationRepository.findByUserId(userId, pageable)
                .map(this::toResponse);
    }

    /**
     * Get notifications by type.
     */
    @Transactional(readOnly = true)
    public Page<NotificationResponse> getNotificationsByType(NotificationType type, Pageable pageable) {

        if (type == null) {
            throw new InvalidNotificationRequestException("Notification type is required");
        }

        return notificationRepository.findByType(type, pageable)
                .map(this::toResponse);
    }

    /**
     * Get notifications by status.
     */
    @Transactional(readOnly = true)
    public Page<NotificationResponse> getNotificationsByStatus(NotificationStatus status, Pageable pageable) {

        if (status == null) {
            throw new InvalidNotificationRequestException("Notification status is required");
        }

        return notificationRepository.findByStatus(status, pageable)
                .map(this::toResponse);
    }

    /**
     * Get all notifications.
     */
    @Transactional(readOnly = true)
    public Page<NotificationResponse> getAllNotifications(Pageable pageable) {

        return notificationRepository.findAll(pageable)
                .map(this::toResponse);
    }

    /**
     * Validate notification request.
     */
    private void validateRequest(NotificationRequest request) {

        if (request == null) {
            throw new InvalidNotificationRequestException("Notification request cannot be null");
        }

        if (request.getUserId() == null) {
            throw new InvalidNotificationRequestException("User ID is required");
        }

        if (request.getType() == null) {
            throw new InvalidNotificationRequestException("Notification type is required");
        }

        if (request.getChannel() == null) {
            throw new InvalidNotificationRequestException("Notification channel is required");
        }

        if (request.getSubject() == null || request.getSubject().isBlank()) {
            throw new InvalidNotificationRequestException("Subject is required");
        }

        if (request.getMessage() == null || request.getMessage().isBlank()) {
            throw new InvalidNotificationRequestException("Message is required");
        }
    }

    /**
     * Entity to response mapper.
     */
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
                .createdAt(notification.getCreatedAt())
                .sentAt(notification.getSentAt())
                .build();
    }
}