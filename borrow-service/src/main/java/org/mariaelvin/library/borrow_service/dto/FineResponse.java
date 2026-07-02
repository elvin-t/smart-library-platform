package org.mariaelvin.library.borrow_service.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class FineResponse {

    private Long borrowRecordId;
    private Long userId;
    private Long bookId;

    private LocalDateTime borrowedAt;
    private LocalDateTime dueDate;
    private LocalDateTime returnedAt;

    private Integer overdueDays;
    private BigDecimal finePerDay;
    private BigDecimal fineAmount;

    private boolean finePaid;
    private LocalDateTime finePaidAt;
}