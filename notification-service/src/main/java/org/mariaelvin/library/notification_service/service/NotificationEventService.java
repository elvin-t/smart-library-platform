package org.mariaelvin.library.notification_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mariaelvin.library.notification_service.entity.Notification;
import org.mariaelvin.library.notification_service.entity.NotificationChannel;
import org.mariaelvin.library.notification_service.entity.NotificationStatus;
import org.mariaelvin.library.notification_service.entity.NotificationType;
import org.mariaelvin.library.notification_service.event.BookBorrowedEvent;
import org.mariaelvin.library.notification_service.event.BookReturnedEvent;
import org.mariaelvin.library.notification_service.event.FinePaidEvent;
import org.mariaelvin.library.notification_service.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationEventService {

    private final NotificationRepository notificationRepository;

    @Transactional
    public void createBorrowConfirmationNotification(BookBorrowedEvent event) {
        if (alreadyExists(event.borrowRecordId(), NotificationType.BORROW_CONFIRMATION)) {
            log.info("Duplicate borrow notification ignored. borrowRecordId={}", event.borrowRecordId());
            return;
        }

        Notification notification = baseNotification(
                event.userId(),
                event.userEmail(),
                event.bookId(),
                event.borrowRecordId()
        );

        notification.setType(NotificationType.BORROW_CONFIRMATION);
        notification.setSubject("Book borrowed successfully");

        String bookName = resolveBookName(event.bookTitle(), event.bookId());

        notification.setMessage(
                "You borrowed " + bookName + ". Due date: " + event.dueDate()
        );

        notificationRepository.save(notification);
    }

    @Transactional
    public void createReturnConfirmationNotification(BookReturnedEvent event) {
        if (alreadyExists(event.borrowRecordId(), NotificationType.RETURN_CONFIRMATION)) {
            log.info("Duplicate return notification ignored. borrowRecordId={}", event.borrowRecordId());
            return;
        }

        Notification notification = baseNotification(
                event.userId(),
                event.userEmail(),
                event.bookId(),
                event.borrowRecordId()
        );

        notification.setType(NotificationType.RETURN_CONFIRMATION);
        notification.setSubject("Book returned successfully");

        String bookName = resolveBookName(event.bookTitle(), event.bookId());

        notification.setMessage(
                "You returned " + bookName +
                        ". Fine amount: " + event.fineAmount() +
                        ". Fine paid: " + event.finePaid()
        );

        notificationRepository.save(notification);
    }

    @Transactional
    public void createFinePaidNotification(FinePaidEvent event) {
        if (alreadyExists(event.borrowRecordId(), NotificationType.FINE_PAID)) {
            log.info("Duplicate fine-paid notification ignored. borrowRecordId={}", event.borrowRecordId());
            return;
        }

        Notification notification = baseNotification(
                event.userId(),
                event.userEmail(),
                event.bookId(),
                event.borrowRecordId()
        );

        notification.setType(NotificationType.FINE_PAID);
        notification.setSubject("Fine payment completed");
        notification.setMessage(
                "Fine paid successfully for borrow record #" +
                        event.borrowRecordId() +
                        ". Amount: " + event.fineAmount()
        );

        notificationRepository.save(notification);
    }

    private Notification baseNotification(
            Long userId,
            String email,
            Long bookId,
            Long borrowRecordId
    ) {
        Notification notification = new Notification();

        notification.setUserId(userId);
        notification.setEmail(email);
        notification.setBookId(bookId);
        notification.setBorrowRecordId(borrowRecordId);
        notification.setChannel(NotificationChannel.IN_APP);
        notification.setStatus(NotificationStatus.SENT);
        notification.setRead(false);

        return notification;
    }

    private boolean alreadyExists(
            Long borrowRecordId,
            NotificationType type
    ) {
        return notificationRepository.existsByBorrowRecordIdAndType(
                borrowRecordId,
                type
        );
    }

    private String resolveBookName(String bookTitle, Long bookId) {
        return bookTitle != null && !bookTitle.isBlank()
                ? bookTitle
                : "Book ID: " + bookId;
    }
}