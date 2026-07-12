package org.mariaelvin.library.borrow_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mariaelvin.library.borrow_service.dto.BookResponse;
import org.mariaelvin.library.borrow_service.dto.BorrowRequest;
import org.mariaelvin.library.borrow_service.dto.BorrowResponse;
import org.mariaelvin.library.borrow_service.dto.FineResponse;
import org.mariaelvin.library.borrow_service.entity.BorrowRecord;
import org.mariaelvin.library.borrow_service.entity.BorrowStatus;
import org.mariaelvin.library.borrow_service.event.BookBorrowedEvent;
import org.mariaelvin.library.borrow_service.event.BookReturnedEvent;
import org.mariaelvin.library.borrow_service.event.FinePaidEvent;
import org.mariaelvin.library.borrow_service.exception.BorrowRecordNotFoundException;
import org.mariaelvin.library.borrow_service.exception.InvalidBorrowRequestException;
import org.mariaelvin.library.borrow_service.kafka.LibraryEventProducer;
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
    private final BookServiceClientFacade bookServiceClientFacade;
    private final LibraryEventProducer libraryEventProducer;

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

        BookResponse book = bookServiceClientFacade.getBookById(request.getBookId());

        if (book == null || !book.isAvailable()) {
            throw new InvalidBorrowRequestException("Book is not available for borrowing");
        }

        boolean inventoryDecreased = false;

        try {
            bookServiceClientFacade.borrowBookCopy(request.getBookId());
            inventoryDecreased = true;

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

            //sendBorrowConfirmation(savedRecord);

            publishBookBorrowedEvent(savedRecord);


            log.info(
                    "Borrow record created and Kafka event published. borrowRecordId={}, userId={}, bookId={}, traceId={}",
                    savedRecord.getId(),
                    savedRecord.getUserId(),
                    savedRecord.getBookId(),
                    getTraceId()
            );


            return toResponse(savedRecord);

        } catch (Exception ex) {

            if (inventoryDecreased) {
                compensateBorrowInventory(request.getBookId());
            }

            throw ex;
        }
    }

    @Transactional
    public BorrowResponse returnBook(Long borrowRecordId) {

        BorrowRecord record = findBorrowRecordById(borrowRecordId);

        if (record.isReturned()) {
            throw new InvalidBorrowRequestException("Book is already returned");
        }

        boolean inventoryIncreased = false;

        try {
            bookServiceClientFacade.returnBookCopy(record.getBookId());
            inventoryIncreased = true;

            LocalDateTime returnedAt = LocalDateTime.now();

            Integer overdueDays = calculateOverdueDays(record.getDueDate(), returnedAt);
            BigDecimal fineAmount = calculateFineAmount(overdueDays);

            record.markReturned(returnedAt, overdueDays, fineAmount);

            BorrowRecord updatedRecord = borrowRecordRepository.save(record);

            publishFinePaidEvent(updatedRecord);


            log.info(
                    "Borrow record returned and Kafka event published. borrowRecordId={}, traceId={}",
                    updatedRecord.getId(),
                    getTraceId()
            );

            return toResponse(updatedRecord);

        } catch (Exception ex) {

            if (inventoryIncreased) {
                compensateReturnInventory(record.getBookId());
            }

            throw ex;
        }
    }

    private void compensateBorrowInventory(Long bookId) {
        try {
            log.warn("Compensating borrow inventory. Returning copy for bookId={}", bookId);
            bookServiceClientFacade.returnBookCopy(bookId);
        } catch (Exception ex) {
            log.error("Borrow compensation failed. Manual reconciliation required. bookId={}", bookId, ex);
        }
    }

    private void compensateReturnInventory(Long bookId) {
        try {
            log.warn("Compensating return inventory. Borrowing copy again for bookId={}", bookId);
            bookServiceClientFacade.borrowBookCopy(bookId);
        } catch (Exception ex) {
            log.error("Return compensation failed. Manual reconciliation required. bookId={}", bookId, ex);
        }
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
            throw new InvalidBorrowRequestException("Fine can be paid only after the book is returned");
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

        publishFinePaidEvent(savedRecord);

        log.info(
                "Fine marked as paid and Kafka event published. borrowRecordId={}, traceId={}",
                savedRecord.getId(),
                getTraceId()
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


    private void publishBookBorrowedEvent(BorrowRecord borrowRecord) {
        BookBorrowedEvent event = new BookBorrowedEvent(
                borrowRecord.getId(),
                borrowRecord.getUserId(),
                borrowRecord.getBookId(),
                getUserEmailSafe(borrowRecord),
                getBookTitleSafe(borrowRecord),
                borrowRecord.getBorrowedAt(),
                borrowRecord.getDueDate(),
                getTraceId()
        );

        libraryEventProducer.publishBookBorrowed(event);
    }

    private void publishBookReturnedEvent(BorrowRecord borrowRecord) {
        BookReturnedEvent event = new BookReturnedEvent(
                borrowRecord.getId(),
                borrowRecord.getUserId(),
                borrowRecord.getBookId(),
                getUserEmailSafe(borrowRecord),
                getBookTitleSafe(borrowRecord),
                borrowRecord.getReturnedAt(),
                borrowRecord.getOverdueDays(),
                borrowRecord.getFineAmount(),
                borrowRecord.isFinePaid(),
                getTraceId()
        );

        libraryEventProducer.publishBookReturned(event);
    }

    private void publishFinePaidEvent(BorrowRecord borrowRecord) {
        FinePaidEvent event = new FinePaidEvent(
                borrowRecord.getId(),
                borrowRecord.getUserId(),
                borrowRecord.getBookId(),
                getUserEmailSafe(borrowRecord),
                borrowRecord.getFineAmount(),
                borrowRecord.getFinePaidAt(),
                getTraceId()
        );

        libraryEventProducer.publishFinePaid(event);
    }

    private String getTraceId() {
        return org.slf4j.MDC.get("traceId");
    }

    private String getUserEmailSafe(BorrowRecord borrowRecord) {
        return null;
    }

    private String getBookTitleSafe(BorrowRecord borrowRecord) {
        return null;
    }


}

