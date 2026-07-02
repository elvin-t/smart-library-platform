package org.mariaelvin.library.borrow_service.repository;

import org.mariaelvin.library.borrow_service.entity.BorrowRecord;
import org.mariaelvin.library.borrow_service.entity.BorrowStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BorrowRecordRepository extends JpaRepository<BorrowRecord, Long> {

    Page<BorrowRecord> findByUserId(Long userId, Pageable pageable);

    Page<BorrowRecord> findByBookId(Long bookId, Pageable pageable);

    Page<BorrowRecord> findByStatus(BorrowStatus status, Pageable pageable);

    Optional<BorrowRecord> findByUserIdAndBookIdAndStatus(
            Long userId,
            Long bookId,
            BorrowStatus status
    );

    boolean existsByUserIdAndBookIdAndStatus(
            Long userId,
            Long bookId,
            BorrowStatus status
    );
}