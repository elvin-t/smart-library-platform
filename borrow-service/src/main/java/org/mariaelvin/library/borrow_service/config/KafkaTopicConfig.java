package org.mariaelvin.library.borrow_service.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Value("${app.kafka.topics.book-borrowed}")
    private String bookBorrowedTopic;

    @Value("${app.kafka.topics.book-returned}")
    private String bookReturnedTopic;

    @Value("${app.kafka.topics.fine-paid}")
    private String finePaidTopic;

    @Bean
    public NewTopic bookBorrowedTopic() {
        return TopicBuilder.name(bookBorrowedTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic bookReturnedTopic() {
        return TopicBuilder.name(bookReturnedTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic finePaidTopic() {
        return TopicBuilder.name(finePaidTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }
}