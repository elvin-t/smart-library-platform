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
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class BorrowService {

    private final BorrowRecordRepository borrowRecordRepository;
    private final BookServiceClientFacade bookServiceClientFacade;
    private final LibraryEventProducer libraryEventProducer;
    private final FineCalculationService fineCalculationService;

    @Value("${app.borrow.default-borrow-days:14}")
    private int defaultBorrowDays;

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

        if (borrowRecordRepository.existsByUserIdAndBookIdAndStatus(
                request.getUserId(),
                request.getBookId(),
                BorrowStatus.OVERDUE
        )) {
            throw new InvalidBorrowRequestException(
                    "User already borrowed this book and it is overdue"
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

            fineCalculationService.calculateFinalFineOnReturn(record, returnedAt);

            record.markReturned(
                    returnedAt,
                    record.getOverdueDays(),
                    record.getFineAmount()
            );

            BorrowRecord updatedRecord = borrowRecordRepository.save(record);

            publishBookReturnedEvent(updatedRecord);

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

    @Transactional
    public BorrowResponse getBorrowRecord(Long id) {
        BorrowRecord record = findBorrowRecordById(id);

        fineCalculationService.updateFineIfRequired(record);

        BorrowRecord savedRecord = borrowRecordRepository.save(record);

        return toResponse(savedRecord);
    }

    @Transactional
    public Page<BorrowResponse> getBorrowRecordsByUser(Long userId, Pageable pageable) {
        Page<BorrowRecord> records = borrowRecordRepository.findByUserId(userId, pageable);

        updateFinesForPage(records);

        return records.map(this::toResponse);
    }

    @Transactional
    public Page<BorrowResponse> getAllBorrowRecords(Pageable pageable) {
        Page<BorrowRecord> records = borrowRecordRepository.findAll(pageable);

        updateFinesForPage(records);

        return records.map(this::toResponse);
    }

    @Transactional
    public Page<BorrowResponse> getBorrowRecordsByStatus(BorrowStatus status, Pageable pageable) {
        Page<BorrowRecord> records = borrowRecordRepository.findByStatus(status, pageable);

        updateFinesForPage(records);

        return records.map(this::toResponse);
    }

    @Transactional
    public FineResponse getFineDetails(Long borrowRecordId) {

        BorrowRecord record = findBorrowRecordById(borrowRecordId);

        fineCalculationService.updateFineIfRequired(record);

        BorrowRecord savedRecord = borrowRecordRepository.save(record);

        return buildFineResponse(savedRecord);
    }

    @Transactional
    public FineResponse markFineAsPaid(Long borrowRecordId) {

        BorrowRecord record = findBorrowRecordById(borrowRecordId);

        if (!record.isReturned()) {
            throw new InvalidBorrowRequestException("Fine can be paid only after the book is returned");
        }

        if (record.getFineAmount() == null ||
                record.getFineAmount().compareTo(BigDecimal.ZERO) <= 0) {
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

        return buildFineResponse(savedRecord);
    }

    private void updateFinesForPage(Page<BorrowRecord> records) {
        records.getContent().forEach(fineCalculationService::updateFineIfRequired);
        borrowRecordRepository.saveAll(records.getContent());
    }

    private FineResponse buildFineResponse(BorrowRecord record) {
        return FineResponse.builder()
                .borrowRecordId(record.getId())
                .userId(record.getUserId())
                .bookId(record.getBookId())
                .borrowedAt(record.getBorrowedAt())
                .dueDate(record.getDueDate())
                .returnedAt(record.getReturnedAt())
                .overdueDays(record.getOverdueDays())
                .finePerDay(fineCalculationService.getFinePerDay())
                .fineAmount(record.getFineAmount())
                .finePaid(record.isFinePaid())
                .finePaidAt(record.getFinePaidAt())
                .build();
    }

    private BorrowRecord findBorrowRecordById(Long id) {
        return borrowRecordRepository.findById(id)
                .orElseThrow(() -> new BorrowRecordNotFoundException(
                        "Borrow record not found with id: " + id
                ));
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