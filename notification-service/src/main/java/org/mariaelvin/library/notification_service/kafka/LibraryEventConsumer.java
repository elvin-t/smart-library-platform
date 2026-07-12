package org.mariaelvin.library.notification_service.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mariaelvin.library.notification_service.event.BookBorrowedEvent;
import org.mariaelvin.library.notification_service.event.BookReturnedEvent;
import org.mariaelvin.library.notification_service.event.FinePaidEvent;
import org.mariaelvin.library.notification_service.service.NotificationEventService;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class LibraryEventConsumer {

    private final NotificationEventService notificationEventService;

    @KafkaListener(
            topics = "${app.kafka.topics.book-borrowed}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consumeBookBorrowedEvent(BookBorrowedEvent event) {
        try {
            putTraceId(event.traceId());

            notificationEventService.createBorrowConfirmationNotification(event);

            log.info(
                    "BookBorrowedEvent processed. borrowRecordId={}, traceId={}",
                    event.borrowRecordId(),
                    event.traceId()
            );

        } finally {
            MDC.clear();
        }
    }

    @KafkaListener(
            topics = "${app.kafka.topics.book-returned}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consumeBookReturnedEvent(BookReturnedEvent event) {
        try {
            putTraceId(event.traceId());

            notificationEventService.createReturnConfirmationNotification(event);

            log.info(
                    "BookReturnedEvent processed. borrowRecordId={}, traceId={}",
                    event.borrowRecordId(),
                    event.traceId()
            );

        } finally {
            MDC.clear();
        }
    }

    @KafkaListener(
            topics = "${app.kafka.topics.fine-paid}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consumeFinePaidEvent(FinePaidEvent event) {
        try {
            putTraceId(event.traceId());

            notificationEventService.createFinePaidNotification(event);

            log.info(
                    "FinePaidEvent processed. borrowRecordId={}, traceId={}",
                    event.borrowRecordId(),
                    event.traceId()
            );

        } finally {
            MDC.clear();
        }
    }

    private void putTraceId(String traceId) {
        if (traceId != null && !traceId.isBlank()) {
            MDC.put("traceId", traceId);
        }
    }
}