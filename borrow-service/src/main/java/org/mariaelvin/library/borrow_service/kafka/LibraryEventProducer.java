package org.mariaelvin.library.borrow_service.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mariaelvin.library.borrow_service.event.BookBorrowedEvent;
import org.mariaelvin.library.borrow_service.event.BookReturnedEvent;
import org.mariaelvin.library.borrow_service.event.FinePaidEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class LibraryEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${app.kafka.topics.book-borrowed}")
    private String bookBorrowedTopic;

    @Value("${app.kafka.topics.book-returned}")
    private String bookReturnedTopic;

    @Value("${app.kafka.topics.fine-paid}")
    private String finePaidTopic;

    public void publishBookBorrowed(BookBorrowedEvent event) {
        String key = String.valueOf(event.borrowRecordId());

        kafkaTemplate.send(bookBorrowedTopic, key, event)
                .whenComplete((result, exception) -> {
                    if (exception != null) {
                        log.error(
                                "Failed to publish BookBorrowedEvent. borrowRecordId={}, traceId={}, reason={}",
                                event.borrowRecordId(),
                                event.traceId(),
                                exception.getMessage(),
                                exception
                        );
                        return;
                    }

                    log.info(
                            "Published BookBorrowedEvent. topic={}, partition={}, offset={}, borrowRecordId={}, traceId={}",
                            result.getRecordMetadata().topic(),
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset(),
                            event.borrowRecordId(),
                            event.traceId()
                    );
                });
    }

    public void publishBookReturned(BookReturnedEvent event) {
        String key = String.valueOf(event.borrowRecordId());

        kafkaTemplate.send(bookReturnedTopic, key, event)
                .whenComplete((result, exception) -> {
                    if (exception != null) {
                        log.error(
                                "Failed to publish BookReturnedEvent. borrowRecordId={}, traceId={}, reason={}",
                                event.borrowRecordId(),
                                event.traceId(),
                                exception.getMessage(),
                                exception
                        );
                        return;
                    }

                    log.info(
                            "Published BookReturnedEvent. topic={}, partition={}, offset={}, borrowRecordId={}, traceId={}",
                            result.getRecordMetadata().topic(),
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset(),
                            event.borrowRecordId(),
                            event.traceId()
                    );
                });
    }

    public void publishFinePaid(FinePaidEvent event) {
        String key = String.valueOf(event.borrowRecordId());

        kafkaTemplate.send(finePaidTopic, key, event)
                .whenComplete((result, exception) -> {
                    if (exception != null) {
                        log.error(
                                "Failed to publish FinePaidEvent. borrowRecordId={}, traceId={}, reason={}",
                                event.borrowRecordId(),
                                event.traceId(),
                                exception.getMessage(),
                                exception
                        );
                        return;
                    }

                    log.info(
                            "Published FinePaidEvent. topic={}, partition={}, offset={}, borrowRecordId={}, traceId={}",
                            result.getRecordMetadata().topic(),
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset(),
                            event.borrowRecordId(),
                            event.traceId()
                    );
                });
    }
}