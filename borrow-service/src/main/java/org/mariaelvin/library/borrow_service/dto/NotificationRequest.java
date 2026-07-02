package org.mariaelvin.library.borrow_service.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NotificationRequest {

    private Long userId;
    private String email;
    private String type;
    private String channel;
    private String subject;
    private String message;
    private Long bookId;
    private Long borrowRecordId;
}