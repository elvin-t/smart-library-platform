package org.mariaelvin.library.notification_service.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record BookReturnedEvent(
        Long borrowRecordId,
        Long userId,
        Long bookId,
        String userEmail,
        String bookTitle,
        LocalDateTime returnedAt,
        Integer overdueDays,
        BigDecimal fineAmount,
        Boolean finePaid,
        String traceId
) {
}