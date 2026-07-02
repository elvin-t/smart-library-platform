package org.mariaelvin.library.borrow_service.dto;

import lombok.Builder;
import lombok.Data;
import org.mariaelvin.library.borrow_service.entity.BorrowStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class BorrowResponse {

    private Long id;
    private Long userId;
    private Long bookId;

    private LocalDateTime borrowedAt;
    private LocalDateTime dueDate;
    private LocalDateTime returnedAt;

    private BorrowStatus status;
    private boolean overdue;

    private Integer overdueDays;
    private BigDecimal fineAmount;
    private boolean finePaid;
    private LocalDateTime finePaidAt;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}