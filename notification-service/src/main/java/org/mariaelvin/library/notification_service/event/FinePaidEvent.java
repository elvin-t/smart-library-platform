package org.mariaelvin.library.notification_service.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record FinePaidEvent(
        Long borrowRecordId,
        Long userId,
        Long bookId,
        String userEmail,
        BigDecimal fineAmount,
        LocalDateTime finePaidAt,
        String traceId
) {
}