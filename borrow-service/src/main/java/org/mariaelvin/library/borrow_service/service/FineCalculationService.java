package org.mariaelvin.library.borrow_service.service;

import lombok.RequiredArgsConstructor;
import org.mariaelvin.library.borrow_service.entity.BorrowRecord;
import org.mariaelvin.library.borrow_service.entity.BorrowStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class FineCalculationService {

    @Value("${app.borrow.fine-per-day:10.00}")
    private BigDecimal finePerDay;

    public void updateFineIfRequired(BorrowRecord record) {
        if (record == null || record.getDueDate() == null) {
            return;
        }

        if (record.isReturned()) {
            keepReturnedFineAsItIs(record);
            return;
        }

        Integer overdueDays = calculateOverdueDays(record.getDueDate(), LocalDateTime.now());
        BigDecimal fineAmount = calculateFineAmount(overdueDays);

        if (overdueDays > 0) {
            record.setStatus(BorrowStatus.OVERDUE);
            record.setOverdueDays(overdueDays);
            record.setFineAmount(fineAmount);
            return;
        }

        markAsNotOverdue(record);
    }

    public void calculateFinalFineOnReturn(BorrowRecord record, LocalDateTime returnedAt) {
        if (record == null || record.getDueDate() == null) {
            return;
        }

        Integer overdueDays = calculateOverdueDays(record.getDueDate(), returnedAt);
        BigDecimal fineAmount = calculateFineAmount(overdueDays);

        record.setOverdueDays(overdueDays);
        record.setFineAmount(fineAmount);
    }

    public Integer calculateOverdueDays(LocalDateTime dueDateTime, LocalDateTime actualDateTime) {
        if (dueDateTime == null || actualDateTime == null) {
            return 0;
        }

        LocalDate dueDate = dueDateTime.toLocalDate();
        LocalDate actualDate = actualDateTime.toLocalDate();

        if (!actualDate.isAfter(dueDate)) {
            return 0;
        }

        long days = ChronoUnit.DAYS.between(dueDate, actualDate);

        return Math.toIntExact(days);
    }

    public BigDecimal calculateFineAmount(Integer overdueDays) {
        if (overdueDays == null || overdueDays <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        return finePerDay
                .multiply(BigDecimal.valueOf(overdueDays))
                .setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal getFinePerDay() {
        return finePerDay.setScale(2, RoundingMode.HALF_UP);
    }

    private void markAsNotOverdue(BorrowRecord record) {
        record.setOverdueDays(0);
        record.setFineAmount(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));

        if (!record.isReturned()) {
            record.setStatus(BorrowStatus.BORROWED);
        }
    }

    private void keepReturnedFineAsItIs(BorrowRecord record) {
        if (record.getOverdueDays() == null) {
            record.setOverdueDays(0);
        }

        if (record.getFineAmount() == null) {
            record.setFineAmount(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        }
    }
}