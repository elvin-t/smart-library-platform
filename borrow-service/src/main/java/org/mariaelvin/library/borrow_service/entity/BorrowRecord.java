package org.mariaelvin.library.borrow_service.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "borrow_records")
public class BorrowRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "book_id", nullable = false)
    private Long bookId;

    @Column(name = "borrowed_at", nullable = false)
    private LocalDateTime borrowedAt;

    @Column(name = "due_date", nullable = false)
    private LocalDateTime dueDate;

    @Column(name = "returned_at")
    private LocalDateTime returnedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private BorrowStatus status;

    @Column(name = "overdue_days", nullable = false)
    private Integer overdueDays = 0;

    @Column(name = "fine_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal fineAmount = BigDecimal.ZERO;

    @Column(name = "fine_paid", nullable = false)
    private boolean finePaid = false;

    @Column(name = "fine_paid_at")
    private LocalDateTime finePaidAt;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    public boolean isReturned() {
        return BorrowStatus.RETURNED.equals(status);
    }

    public boolean isOverdue() {
        return BorrowStatus.BORROWED.equals(status)
                && dueDate != null
                && dueDate.isBefore(LocalDateTime.now());
    }

    public void markReturned(LocalDateTime returnedAt,
                             Integer overdueDays,
                             BigDecimal fineAmount) {

        this.status = BorrowStatus.RETURNED;
        this.returnedAt = returnedAt;
        this.overdueDays = overdueDays == null ? 0 : overdueDays;
        this.fineAmount = fineAmount == null ? BigDecimal.ZERO : fineAmount;

        if (this.fineAmount.compareTo(BigDecimal.ZERO) == 0) {
            this.finePaid = true;
            this.finePaidAt = returnedAt;
        }
    }

    public void markFinePaid() {
        this.finePaid = true;
        this.finePaidAt = LocalDateTime.now();
    }
}