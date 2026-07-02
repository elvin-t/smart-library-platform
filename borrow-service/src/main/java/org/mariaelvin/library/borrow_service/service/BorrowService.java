package org.mariaelvin.library.borrow_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mariaelvin.library.borrow_service.client.BookClient;
import org.mariaelvin.library.borrow_service.dto.BookResponse;
import org.mariaelvin.library.borrow_service.dto.BorrowRequest;
import org.mariaelvin.library.borrow_service.dto.BorrowResponse;
import org.mariaelvin.library.borrow_service.entity.BorrowRecord;
import org.mariaelvin.library.borrow_service.entity.BorrowStatus;
import org.mariaelvin.library.borrow_service.exception.BorrowRecordNotFoundException;
import org.mariaelvin.library.borrow_service.exception.InvalidBorrowRequestException;
import org.mariaelvin.library.borrow_service.repository.BorrowRecordRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.beans.factory.annotation.Value;
import org.mariaelvin.library.borrow_service.client.NotificationClient;
import org.mariaelvin.library.borrow_service.dto.NotificationRequest;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class BorrowService {

    private static final int DEFAULT_BORROW_DAYS = 14;

    private final BorrowRecordRepository borrowRecordRepository;
    private final BookClient bookClient;
    private final NotificationClient notificationClient;

    @Value("${app.internal.token}")
    private String internalToken;

    @Transactional
    public BorrowResponse borrowBook(BorrowRequest request) {

        if (borrowRecordRepository.existsByUserIdAndBookIdAndStatus(
                request.getUserId(),
                request.getBookId(),
                BorrowStatus.BORROWED
        )) {
            throw new InvalidBorrowRequestException("User already borrowed this book and has not returned it");
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
                .dueDate(now.plusDays(DEFAULT_BORROW_DAYS))
                .status(BorrowStatus.BORROWED)
                .build();

        BorrowRecord savedRecord = borrowRecordRepository.save(record);

        sendBorrowConfirmation(savedRecord);

        return toResponse(savedRecord);
    }

    @Transactional
    public BorrowResponse returnBook(Long borrowRecordId) {

        BorrowRecord record = borrowRecordRepository.findById(borrowRecordId)
                .orElseThrow(() -> new BorrowRecordNotFoundException(
                        "Borrow record not found with id: " + borrowRecordId
                ));

        if (record.isReturned()) {
            throw new InvalidBorrowRequestException("Book is already returned");
        }

        bookClient.returnBookCopy(record.getBookId());

        record.markReturned();

        BorrowRecord updatedRecord = borrowRecordRepository.save(record);

        sendReturnConfirmation(updatedRecord);

        return toResponse(updatedRecord);
    }

    @Transactional(readOnly = true)
    public BorrowResponse getBorrowRecord(Long id) {
        BorrowRecord record = borrowRecordRepository.findById(id)
                .orElseThrow(() -> new BorrowRecordNotFoundException(
                        "Borrow record not found with id: " + id
                ));

        return toResponse(record);
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
                .createdAt(record.getCreatedAt())
                .updatedAt(record.getUpdatedAt())
                .build();
    }


    private void sendReturnConfirmation(BorrowRecord record) {

        try {
            NotificationRequest notificationRequest = NotificationRequest.builder()
                    .userId(record.getUserId())
                    .email(null)
                    .type("RETURN_CONFIRMATION")
                    .channel("EMAIL")
                    .subject("Book returned successfully")
                    .message("You have successfully returned book ID " + record.getBookId()
                            + ". Returned at: " + record.getReturnedAt())
                    .bookId(record.getBookId())
                    .borrowRecordId(record.getId())
                    .build();

            notificationClient.sendNotification(internalToken, notificationRequest);

        } catch (Exception ex) {
            log.warn("Failed to send return confirmation notification. borrowRecordId={}, reason={}",
                    record.getId(),
                    ex.getMessage());
        }
    }

    private void sendBorrowConfirmation(BorrowRecord record) {

        try {
            NotificationRequest notificationRequest = NotificationRequest.builder()
                    .userId(record.getUserId())
                    .email(null)
                    .type("BORROW_CONFIRMATION")
                    .channel("EMAIL")
                    .subject("Book borrowed successfully")
                    .message("You have successfully borrowed book ID " + record.getBookId()
                            + ". Due date: " + record.getDueDate())
                    .bookId(record.getBookId())
                    .borrowRecordId(record.getId())
                    .build();

            notificationClient.sendNotification(internalToken, notificationRequest);

        } catch (Exception ex) {
            log.warn("Failed to send borrow confirmation notification. borrowRecordId={}, reason={}",
                    record.getId(),
                    ex.getMessage());
        }
    }
}