package org.mariaelvin.library.borrow_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mariaelvin.library.borrow_service.client.BookClient;
import org.mariaelvin.library.borrow_service.client.NotificationClient;
import org.mariaelvin.library.borrow_service.dto.BookResponse;
import org.mariaelvin.library.borrow_service.dto.BorrowRequest;
import org.mariaelvin.library.borrow_service.dto.BorrowResponse;
import org.mariaelvin.library.borrow_service.dto.FineResponse;
import org.mariaelvin.library.borrow_service.dto.NotificationRequest;
import org.mariaelvin.library.borrow_service.entity.BorrowRecord;
import org.mariaelvin.library.borrow_service.entity.BorrowStatus;
import org.mariaelvin.library.borrow_service.exception.BorrowRecordNotFoundException;
import org.mariaelvin.library.borrow_service.exception.InvalidBorrowRequestException;
import org.mariaelvin.library.borrow_service.repository.BorrowRecordRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class BorrowService {

    private final BorrowRecordRepository borrowRecordRepository;
    private final BookClient bookClient;
    private final NotificationClient notificationClient;

    @Value("${app.internal.token}")
    private String internalToken;

    @Value("${app.borrow.default-borrow-days:14}")
    private int defaultBorrowDays;

    @Value("${app.borrow.fine-per-day:10.00}")
    private BigDecimal finePerDay;

    @Transactional
    public BorrowResponse borrowBook(BorrowRequest request) {

        if (borrowRecordRepository.existsByUserIdAndBookIdAndStatus(
                request.getUserId(),
                request.getBookId(),
                BorrowStatus.BORROWED
        )) {
            throw new InvalidBorrowRequestException(
                    "User already borrowed this book and has not returned it"
            );
        }

        BookResponse book = bookClient.getBookById(request.getBookId());

        if (book == null || !book.isAvailable()) {
            throw new InvalidBorrowRequestException("Book is not available for borrowing");
        }

        bookClient.borrowBookCopy(request.getBookId());

        LocalDateTime now = LocalDateTime.now();

        BorrowRecord record = BorrowRecord.builder()
                .userId(request.getUserId())
                .bookId(request.getBookId())
                .borrowedAt(now)
                .dueDate(now.plusDays(defaultBorrowDays))
                .status(BorrowStatus.BORROWED)
                .overdueDays(0)
                .fineAmount(BigDecimal.ZERO)
                .finePaid(false)
                .build();

        BorrowRecord savedRecord = borrowRecordRepository.save(record);

        sendBorrowConfirmation(savedRecord);

        return toResponse(savedRecord);
    }

    @Transactional
    public BorrowResponse returnBook(Long borrowRecordId) {

        BorrowRecord record = findBorrowRecordById(borrowRecordId);

        if (record.isReturned()) {
            throw new InvalidBorrowRequestException("Book is already returned");
        }

        bookClient.returnBookCopy(record.getBookId());

        LocalDateTime returnedAt = LocalDateTime.now();

        Integer overdueDays = calculateOverdueDays(record.getDueDate(), returnedAt);
        BigDecimal fineAmount = calculateFineAmount(overdueDays);

        record.markReturned(returnedAt, overdueDays, fineAmount);

        BorrowRecord updatedRecord = borrowRecordRepository.save(record);

        log.info(
                "Book returned successfully. borrowRecordId={}, overdueDays={}, fineAmount={}, finePaid={}",
                updatedRecord.getId(),
                updatedRecord.getOverdueDays(),
                updatedRecord.getFineAmount(),
                updatedRecord.isFinePaid()
        );

        sendReturnConfirmation(updatedRecord);

        return toResponse(updatedRecord);
    }

    @Transactional(readOnly = true)
    public BorrowResponse getBorrowRecord(Long id) {
        return toResponse(findBorrowRecordById(id));
    }

    @Transactional(readOnly = true)
    public Page<BorrowResponse> getBorrowRecordsByUser(Long userId, Pageable pageable) {
        return borrowRecordRepository.findByUserId(userId, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<BorrowResponse> getAllBorrowRecords(Pageable pageable) {
        return borrowRecordRepository.findAll(pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<BorrowResponse> getBorrowRecordsByStatus(BorrowStatus status, Pageable pageable) {
        return borrowRecordRepository.findByStatus(status, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public FineResponse getFineDetails(Long borrowRecordId) {

        BorrowRecord record = findBorrowRecordById(borrowRecordId);

        Integer overdueDays;
        BigDecimal fineAmount;

        if (record.isReturned()) {
            overdueDays = record.getOverdueDays();
            fineAmount = record.getFineAmount();
        } else {
            overdueDays = calculateOverdueDays(record.getDueDate(), LocalDateTime.now());
            fineAmount = calculateFineAmount(overdueDays);
        }

        return FineResponse.builder()
                .borrowRecordId(record.getId())
                .userId(record.getUserId())
                .bookId(record.getBookId())
                .borrowedAt(record.getBorrowedAt())
                .dueDate(record.getDueDate())
                .returnedAt(record.getReturnedAt())
                .overdueDays(overdueDays)
                .finePerDay(finePerDay)
                .fineAmount(fineAmount)
                .finePaid(record.isFinePaid())
                .finePaidAt(record.getFinePaidAt())
                .build();
    }

    @Transactional
    public FineResponse markFineAsPaid(Long borrowRecordId) {

        BorrowRecord record = findBorrowRecordById(borrowRecordId);

        if (!record.isReturned()) {
            throw new InvalidBorrowRequestException(
                    "Fine can be paid only after the book is returned"
            );
        }

        if (record.getFineAmount() == null ||
                record.getFineAmount().compareTo(BigDecimal.ZERO) == 0) {
            throw new InvalidBorrowRequestException("No fine is pending for this borrow record");
        }

        if (record.isFinePaid()) {
            throw new InvalidBorrowRequestException("Fine is already paid");
        }

        record.markFinePaid();

        BorrowRecord savedRecord = borrowRecordRepository.save(record);

        log.info(
                "Fine marked as paid. borrowRecordId={}, fineAmount={}, paidAt={}",
                savedRecord.getId(),
                savedRecord.getFineAmount(),
                savedRecord.getFinePaidAt()
        );

        return FineResponse.builder()
                .borrowRecordId(savedRecord.getId())
                .userId(savedRecord.getUserId())
                .bookId(savedRecord.getBookId())
                .borrowedAt(savedRecord.getBorrowedAt())
                .dueDate(savedRecord.getDueDate())
                .returnedAt(savedRecord.getReturnedAt())
                .overdueDays(savedRecord.getOverdueDays())
                .finePerDay(finePerDay)
                .fineAmount(savedRecord.getFineAmount())
                .finePaid(savedRecord.isFinePaid())
                .finePaidAt(savedRecord.getFinePaidAt())
                .build();
    }

    private BorrowRecord findBorrowRecordById(Long id) {
        return borrowRecordRepository.findById(id)
                .orElseThrow(() -> new BorrowRecordNotFoundException(
                        "Borrow record not found with id: " + id
                ));
    }

    private Integer calculateOverdueDays(LocalDateTime dueDate, LocalDateTime actualDateTime) {

        if (dueDate == null || actualDateTime == null || !actualDateTime.isAfter(dueDate)) {
            return 0;
        }

        Duration overdueDuration = Duration.between(dueDate, actualDateTime);

        long seconds = overdueDuration.getSeconds();
        long days = seconds / 86400;

        if (seconds % 86400 != 0) {
            days++;
        }

        return Math.toIntExact(days);
    }

    private BigDecimal calculateFineAmount(Integer overdueDays) {

        if (overdueDays == null || overdueDays <= 0) {
            return BigDecimal.ZERO;
        }

        return finePerDay
                .multiply(BigDecimal.valueOf(overdueDays))
                .setScale(2, RoundingMode.HALF_UP);
    }

    private BorrowResponse toResponse(BorrowRecord record) {

        return BorrowResponse.builder()
                .id(record.getId())
                .userId(record.getUserId())
                .bookId(record.getBookId())
                .borrowedAt(record.getBorrowedAt())
                .dueDate(record.getDueDate())
                .returnedAt(record.getReturnedAt())
                .status(record.getStatus())
                .overdue(record.isOverdue())
                .overdueDays(record.getOverdueDays())
                .fineAmount(record.getFineAmount())
                .finePaid(record.isFinePaid())
                .finePaidAt(record.getFinePaidAt())
                .createdAt(record.getCreatedAt())
                .updatedAt(record.getUpdatedAt())
                .build();
    }

    private void sendBorrowConfirmation(BorrowRecord record) {

        try {
            NotificationRequest notificationRequest = NotificationRequest.builder()
                    .userId(record.getUserId())
                    .email(null)
                    .type("BORROW_CONFIRMATION")
                    .channel("EMAIL")
                    .subject("Book borrowed successfully")
                    .message("You have successfully borrowed book ID "
                            + record.getBookId()
                            + ". Due date: "
                            + record.getDueDate())
                    .bookId(record.getBookId())
                    .borrowRecordId(record.getId())
                    .build();

            notificationClient.sendNotification(internalToken, notificationRequest);

        } catch (Exception ex) {
            log.warn(
                    "Failed to send borrow confirmation notification. borrowRecordId={}, reason={}",
                    record.getId(),
                    ex.getMessage()
            );
        }
    }

    private void sendReturnConfirmation(BorrowRecord record) {

        try {
            String message = "You have successfully returned book ID "
                    + record.getBookId()
                    + ". Returned at: "
                    + record.getReturnedAt()
                    + ". Overdue days: "
                    + record.getOverdueDays()
                    + ". Fine amount: "
                    + record.getFineAmount();

            NotificationRequest notificationRequest = NotificationRequest.builder()
                    .userId(record.getUserId())
                    .email(null)
                    .type("RETURN_CONFIRMATION")
                    .channel("EMAIL")
                    .subject("Book returned successfully")
                    .message(message)
                    .bookId(record.getBookId())
                    .borrowRecordId(record.getId())
                    .build();

            notificationClient.sendNotification(internalToken, notificationRequest);

        } catch (Exception ex) {
            log.warn(
                    "Failed to send return confirmation notification. borrowRecordId={}, reason={}",
                    record.getId(),
                    ex.getMessage()
            );
        }
    }
}