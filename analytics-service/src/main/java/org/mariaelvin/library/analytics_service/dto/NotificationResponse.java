package org.mariaelvin.library.analytics_service.dto;

import lombok.Data;

@Data
public class NotificationResponse {

    private Long id;
    private Long userId;
    private String type;
    private String status;
    private Boolean read;
}