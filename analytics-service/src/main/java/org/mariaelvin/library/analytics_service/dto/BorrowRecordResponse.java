package org.mariaelvin.library.analytics_service.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class BorrowRecordResponse {

    private Long id;
    private Long userId;
    private Long bookId;

    private String status;

    private Integer overdueDays;
    private BigDecimal fineAmount;
    private Boolean finePaid;
}