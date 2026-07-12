package org.mariaelvin.library.borrow_service.event;

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