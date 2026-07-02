package org.mariaelvin.library.book_service.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.mariaelvin.library.book_service.dto.AddCopiesRequest;
import org.mariaelvin.library.book_service.dto.AdjustAvailableCopiesRequest;
import org.mariaelvin.library.book_service.dto.InventoryResponse;
import org.mariaelvin.library.book_service.dto.RemoveCopiesRequest;
import org.mariaelvin.library.book_service.service.InventoryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/books/inventory")
@RequiredArgsConstructor
@Tag(name = "Inventory APIs", description = "Inventory management APIs inside Book Service")
public class InventoryController {

    private final InventoryService inventoryService;

    @Operation(summary = "Get inventory by book ID")
    @PreAuthorize("hasAuthority('INVENTORY_READ')")
    @GetMapping("/{bookId}")
    public ResponseEntity<InventoryResponse> getInventoryByBookId(@PathVariable Long bookId) {

        return ResponseEntity.ok(inventoryService.getInventoryByBookId(bookId));
    }

    @Operation(summary = "Add copies to a book")
    @PreAuthorize("hasAuthority('INVENTORY_WRITE')")
    @PatchMapping("/{bookId}/add-copies")
    public ResponseEntity<InventoryResponse> addCopies(
            @PathVariable Long bookId,
            @Valid @RequestBody AddCopiesRequest request) {

        return ResponseEntity.ok(inventoryService.addCopies(bookId, request));
    }

    @Operation(summary = "Remove copies from a book")
    @PreAuthorize("hasAuthority('INVENTORY_WRITE')")
    @PatchMapping("/{bookId}/remove-copies")
    public ResponseEntity<InventoryResponse> removeCopies(
            @PathVariable Long bookId,
            @Valid @RequestBody RemoveCopiesRequest request) {

        return ResponseEntity.ok(inventoryService.removeCopies(bookId, request));
    }

    @Operation(summary = "Adjust available copies")
    @PreAuthorize("hasAuthority('INVENTORY_WRITE')")
    @PatchMapping("/{bookId}/available-copies")
    public ResponseEntity<InventoryResponse> adjustAvailableCopies(
            @PathVariable Long bookId,
            @Valid @RequestBody AdjustAvailableCopiesRequest request) {

        return ResponseEntity.ok(
                inventoryService.adjustAvailableCopies(bookId, request)
        );
    }

    @Operation(summary = "Get unavailable books")
    @PreAuthorize("hasAuthority('INVENTORY_READ')")
    @GetMapping("/unavailable")
    public ResponseEntity<Page<InventoryResponse>> getUnavailableBooks(
            @PageableDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {

        return ResponseEntity.ok(inventoryService.getUnavailableBooks(pageable));
    }

    @Operation(summary = "Get low stock books")
    @PreAuthorize("hasAuthority('INVENTORY_READ')")
    @GetMapping("/low-stock")
    public ResponseEntity<Page<InventoryResponse>> getLowStockBooks(
            @RequestParam(defaultValue = "2") Integer threshold,
            @PageableDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {

        return ResponseEntity.ok(
                inventoryService.getLowStockBooks(threshold, pageable)
        );
    }
}