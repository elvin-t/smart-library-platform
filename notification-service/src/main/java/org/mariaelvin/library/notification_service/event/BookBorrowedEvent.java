package org.mariaelvin.library.notification_service.event;

import java.time.LocalDateTime;

public record BookBorrowedEvent(
        Long borrowRecordId,
        Long userId,
        Long bookId,
        String userEmail,
        String bookTitle,
        LocalDateTime borrowedAt,
        LocalDateTime dueDate,
        String traceId
) {
}