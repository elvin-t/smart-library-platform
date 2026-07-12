package org.mariaelvin.library.notification_service.repository;

import org.mariaelvin.library.notification_service.entity.Notification;
import org.mariaelvin.library.notification_service.entity.NotificationStatus;
import org.mariaelvin.library.notification_service.entity.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findByUserId(Long userId, Pageable pageable);

    Page<Notification> findByType(NotificationType type, Pageable pageable);

    Page<Notification> findByStatus(NotificationStatus status, Pageable pageable);

    Long countByUserIdAndReadFalse(Long userId);

    boolean existsByBorrowRecordIdAndType(
            Long borrowRecordId,
            NotificationType type
    );

    @Modifying
    @Query("""
            UPDATE Notification n
            SET n.read = true,
                n.readAt = :readAt
            WHERE n.userId = :userId
              AND n.read = false
            """)
    void markAllAsReadByUserId(
            Long userId,
            LocalDateTime readAt
    );
}