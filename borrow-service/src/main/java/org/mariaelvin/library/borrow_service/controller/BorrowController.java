package org.mariaelvin.library.borrow_service.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.mariaelvin.library.borrow_service.dto.BorrowRequest;
import org.mariaelvin.library.borrow_service.dto.BorrowResponse;
import org.mariaelvin.library.borrow_service.dto.FineResponse;
import org.mariaelvin.library.borrow_service.entity.BorrowStatus;
import org.mariaelvin.library.borrow_service.service.BorrowService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/borrow-records")
@RequiredArgsConstructor
@Tag(name = "Borrow APIs", description = "Book borrowing and return APIs")
public class BorrowController {

    private final BorrowService borrowService;

    @Operation(summary = "Borrow a book")
    @PreAuthorize("hasAuthority('BORROW_WRITE')")
    @PostMapping
    public ResponseEntity<BorrowResponse> borrowBook(@Valid @RequestBody BorrowRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(borrowService.borrowBook(request));
    }

    @Operation(summary = "Return a borrowed book")
    @PreAuthorize("hasAuthority('RETURN_WRITE')")
    @PatchMapping("/{borrowRecordId}/return")
    public ResponseEntity<BorrowResponse> returnBook(@PathVariable Long borrowRecordId) {
        return ResponseEntity.ok(borrowService.returnBook(borrowRecordId));
    }


    @Operation(summary = "Get fine details for borrow record")
    @PreAuthorize("hasAuthority('BORROW_READ')")
    @GetMapping("/{borrowRecordId}/fine")
    public ResponseEntity<FineResponse> getFineDetails(@PathVariable Long borrowRecordId) {
        return ResponseEntity.ok(borrowService.getFineDetails(borrowRecordId));
    }


    @Operation(summary = "Mark fine as paid")
    @PreAuthorize("hasAuthority('RETURN_WRITE')")
    @PatchMapping("/{borrowRecordId}/fine/pay")
    public ResponseEntity<FineResponse> markFineAsPaid(@PathVariable Long borrowRecordId) {
        return ResponseEntity.ok(borrowService.markFineAsPaid(borrowRecordId));
    }



    @Operation(summary = "Get borrow record by ID")
    @PreAuthorize("hasAuthority('BORROW_READ')")
    @GetMapping("/{id}")
    public ResponseEntity<BorrowResponse> getBorrowRecord(@PathVariable Long id) {
        return ResponseEntity.ok(borrowService.getBorrowRecord(id));
    }

    @Operation(summary = "Get borrow records by user")
    @PreAuthorize("hasAuthority('BORROW_READ')")
    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<BorrowResponse>> getBorrowRecordsByUser(
            @PathVariable Long userId,
            Pageable pageable) {

        return ResponseEntity.ok(borrowService.getBorrowRecordsByUser(userId, pageable));
    }

    @Operation(summary = "Get all borrow records")
    @PreAuthorize("hasAuthority('BORROW_READ')")
    @GetMapping
    public ResponseEntity<Page<BorrowResponse>> getAllBorrowRecords(Pageable pageable) {
        return ResponseEntity.ok(borrowService.getAllBorrowRecords(pageable));
    }

    @Operation(summary = "Get borrow records by status")
    @PreAuthorize("hasAuthority('BORROW_READ')")
    @GetMapping("/status/{status}")
    public ResponseEntity<Page<BorrowResponse>> getBorrowRecordsByStatus(
            @PathVariable BorrowStatus status,
            Pageable pageable) {

        return ResponseEntity.ok(borrowService.getBorrowRecordsByStatus(status, pageable));
    }

}