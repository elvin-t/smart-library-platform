package org.mariaelvin.library.book_service.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.mariaelvin.library.book_service.dto.BookResponse;
import org.mariaelvin.library.book_service.dto.CreateBookRequest;
import org.mariaelvin.library.book_service.dto.UpdateBookRequest;
import org.mariaelvin.library.book_service.dto.BookGenre;
import org.mariaelvin.library.book_service.service.BookService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
@Tag(name = "Book APIs", description = "Book catalog management APIs")
public class BookController {

    private final BookService bookService;

    @Operation(summary = "Create a new book")
    @PreAuthorize("hasAuthority('BOOK_WRITE')")
    @PostMapping
    public ResponseEntity<BookResponse> createBook(@Valid @RequestBody CreateBookRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(bookService.createBook(request));
    }

    @Operation(summary = "Get book by ID")
    @PreAuthorize("hasAuthority('BOOK_READ')")
    @GetMapping("/{id}")
    public ResponseEntity<BookResponse> getBookById(@PathVariable Long id) {
        return ResponseEntity.ok(bookService.getBookById(id));
    }

    @Operation(summary = "Get book by ISBN")
    @PreAuthorize("hasAuthority('BOOK_READ')")
    @GetMapping("/isbn/{isbn}")
    public ResponseEntity<BookResponse> getBookByIsbn(@PathVariable String isbn) {
        return ResponseEntity.ok(bookService.getBookByIsbn(isbn));
    }

    @Operation(summary = "Get all books")
    @PreAuthorize("hasAuthority('BOOK_READ')")
    @GetMapping
    public ResponseEntity<Page<BookResponse>> getAllBooks(Pageable pageable) {
        return ResponseEntity.ok(bookService.getAllBooks(pageable));
    }

    @Operation(summary = "Search books by title or author")
    @PreAuthorize("hasAuthority('BOOK_READ')")
    @GetMapping("/search")
    public ResponseEntity<Page<BookResponse>> searchBooks(
            @RequestParam String keyword,
            Pageable pageable) {

        return ResponseEntity.ok(bookService.searchBooks(keyword, pageable));
    }

    @Operation(summary = "Get books by genre")
    @PreAuthorize("hasAuthority('BOOK_READ')")
    @GetMapping("/genre/{genre}")
    public ResponseEntity<Page<BookResponse>> getBooksByGenre(
            @PathVariable BookGenre genre,
            Pageable pageable) {

        return ResponseEntity.ok(bookService.getBooksByGenre(genre, pageable));
    }

    @Operation(summary = "Get available books")
    @PreAuthorize("hasAuthority('BOOK_READ')")
    @GetMapping("/available")
    public ResponseEntity<Page<BookResponse>> getAvailableBooks(Pageable pageable) {
        return ResponseEntity.ok(bookService.getAvailableBooks(pageable));
    }

    @Operation(summary = "Update book details")
    @PreAuthorize("hasAuthority('BOOK_WRITE')")
    @PutMapping("/{id}")
    public ResponseEntity<BookResponse> updateBook(
            @PathVariable Long id,
            @Valid @RequestBody UpdateBookRequest request) {

        return ResponseEntity.ok(bookService.updateBook(id, request));
    }

    @Operation(summary = "Delete book")
    @PreAuthorize("hasAuthority('BOOK_WRITE')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Optional APIs.
     * Later, Circulation Service can call these as internal APIs.
     */
    @Operation(summary = "Borrow one copy of a book")
    @PreAuthorize("hasAuthority('BORROW_WRITE')")
    @PostMapping("/{id}/borrow")
    public ResponseEntity<BookResponse> borrowBookCopy(@PathVariable Long id) {
        return ResponseEntity.ok(bookService.borrowBookCopy(id));
    }

    @Operation(summary = "Return one copy of a book")
    @PreAuthorize("hasAuthority('RETURN_WRITE')")
    @PostMapping("/{id}/return")
    public ResponseEntity<BookResponse> returnBookCopy(@PathVariable Long id) {
        return ResponseEntity.ok(bookService.returnBookCopy(id));
    }
}