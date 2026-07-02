package org.mariaelvin.library.notification_service.repository;

import org.mariaelvin.library.notification_service.entity.Notification;
import org.mariaelvin.library.notification_service.entity.NotificationStatus;
import org.mariaelvin.library.notification_service.entity.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findByUserId(Long userId, Pageable pageable);

    Page<Notification> findByType(NotificationType type, Pageable pageable);

    Page<Notification> findByStatus(NotificationStatus status, Pageable pageable);
}