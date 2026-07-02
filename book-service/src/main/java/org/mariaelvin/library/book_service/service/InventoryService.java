package org.mariaelvin.library.book_service.service;

import lombok.RequiredArgsConstructor;
import org.mariaelvin.library.book_service.dto.AddCopiesRequest;
import org.mariaelvin.library.book_service.dto.AdjustAvailableCopiesRequest;
import org.mariaelvin.library.book_service.dto.InventoryResponse;
import org.mariaelvin.library.book_service.dto.RemoveCopiesRequest;
import org.mariaelvin.library.book_service.entity.Book;
import org.mariaelvin.library.book_service.exception.BookNotFoundException;
import org.mariaelvin.library.book_service.exception.InvalidBookRequestException;
import org.mariaelvin.library.book_service.repository.BookRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final BookRepository bookRepository;

    @Transactional(readOnly = true)
    public InventoryResponse getInventoryByBookId(Long bookId) {
        Book book = findBookById(bookId);
        return toInventoryResponse(book);
    }

    @Transactional
    public InventoryResponse addCopies(Long bookId, AddCopiesRequest request) {
        Book book = findBookById(bookId);

        try {
            book.addCopies(request.getCopies());
        } catch (IllegalArgumentException ex) {
            throw new InvalidBookRequestException(ex.getMessage());
        }

        Book updatedBook = bookRepository.save(book);

        return toInventoryResponse(updatedBook);
    }

    @Transactional
    public InventoryResponse removeCopies(Long bookId, RemoveCopiesRequest request) {
        Book book = findBookById(bookId);

        try {
            book.removeCopies(request.getCopies());
        } catch (IllegalArgumentException ex) {
            throw new InvalidBookRequestException(ex.getMessage());
        }

        Book updatedBook = bookRepository.save(book);

        return toInventoryResponse(updatedBook);
    }

    @Transactional
    public InventoryResponse adjustAvailableCopies(
            Long bookId,
            AdjustAvailableCopiesRequest request) {

        Book book = findBookById(bookId);

        try {
            book.adjustAvailableCopies(request.getAvailableCopies());
        } catch (IllegalArgumentException ex) {
            throw new InvalidBookRequestException(ex.getMessage());
        }

        Book updatedBook = bookRepository.save(book);

        return toInventoryResponse(updatedBook);
    }

    @Transactional(readOnly = true)
    public Page<InventoryResponse> getUnavailableBooks(Pageable pageable) {
        return bookRepository.findByAvailableCopiesEquals(0, pageable)
                .map(this::toInventoryResponse);
    }

    @Transactional(readOnly = true)
    public Page<InventoryResponse> getLowStockBooks(Integer threshold, Pageable pageable) {

        if (threshold == null || threshold < 0) {
            throw new InvalidBookRequestException("Threshold cannot be negative");
        }

        return bookRepository.findByAvailableCopiesLessThanEqual(threshold, pageable)
                .map(this::toInventoryResponse);
    }

    private Book findBookById(Long bookId) {
        return bookRepository.findById(bookId)
                .orElseThrow(() -> new BookNotFoundException("Book not found with id: " + bookId));
    }

    private InventoryResponse toInventoryResponse(Book book) {
        return InventoryResponse.builder()
                .bookId(book.getId())
                .isbn(book.getIsbn())
                .title(book.getTitle())
                .author(book.getAuthor())
                .totalCopies(book.getTotalCopies())
                .availableCopies(book.getAvailableCopies())
                .borrowedCopies(book.getBorrowedCopies())
                .available(book.isAvailable())
                .build();
    }
}