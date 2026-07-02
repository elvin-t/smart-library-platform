package org.mariaelvin.library.book_service.service;

import lombok.RequiredArgsConstructor;
import org.mariaelvin.library.book_service.dto.BookResponse;
import org.mariaelvin.library.book_service.dto.CreateBookRequest;
import org.mariaelvin.library.book_service.dto.UpdateBookRequest;
import org.mariaelvin.library.book_service.entity.Book;
import org.mariaelvin.library.book_service.dto.BookGenre;
import org.mariaelvin.library.book_service.exception.BookAlreadyExistsException;
import org.mariaelvin.library.book_service.exception.BookNotFoundException;
import org.mariaelvin.library.book_service.exception.InvalidBookRequestException;
import org.mariaelvin.library.book_service.repository.BookRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;

    @Transactional
    public BookResponse createBook(CreateBookRequest request) {

        if (bookRepository.existsByIsbn(request.getIsbn())) {
            throw new BookAlreadyExistsException("Book already exists with ISBN: " + request.getIsbn());
        }

        Integer totalCopies = request.getTotalCopies();
        Integer availableCopies = request.getAvailableCopies() == null
                ? totalCopies
                : request.getAvailableCopies();

        validateCopies(totalCopies, availableCopies);

        Book book = Book.builder()
                .isbn(request.getIsbn())
                .title(request.getTitle())
                .author(request.getAuthor())
                .description(request.getDescription())
                .genre(request.getGenre())
                .totalCopies(totalCopies)
                .availableCopies(availableCopies)
                .publicationDate(request.getPublicationDate())
                .build();

        return toResponse(bookRepository.save(book));
    }

    @Transactional(readOnly = true)
    public BookResponse getBookById(Long id) {
        return toResponse(findBookById(id));
    }

    @Transactional(readOnly = true)
    public BookResponse getBookByIsbn(String isbn) {
        Book book = bookRepository.findByIsbn(isbn)
                .orElseThrow(() -> new BookNotFoundException("Book not found with ISBN: " + isbn));

        return toResponse(book);
    }

    @Transactional(readOnly = true)
    public Page<BookResponse> getAllBooks(Pageable pageable) {
        return bookRepository.findAll(pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<BookResponse> getBooksByGenre(BookGenre genre, Pageable pageable) {
        return bookRepository.findByGenre(genre, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<BookResponse> getAvailableBooks(Pageable pageable) {
        return bookRepository.findByAvailableCopiesGreaterThan(0, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<BookResponse> searchBooks(String keyword, Pageable pageable) {
        if (keyword == null || keyword.isBlank()) {
            return getAllBooks(pageable);
        }

        return bookRepository.searchByFullText(keyword.trim(), pageable)
                .map(this::toResponse);
    }

    @Transactional
    public BookResponse updateBook(Long id, UpdateBookRequest request) {

        Book book = findBookById(id);

        validateCopies(request.getTotalCopies(), request.getAvailableCopies());

        book.updateDetails(
                request.getTitle(),
                request.getAuthor(),
                request.getDescription(),
                request.getGenre(),
                request.getPublicationDate()
        );

        book.updateCopies(request.getTotalCopies(), request.getAvailableCopies());

        return toResponse(bookRepository.save(book));
    }

    @Transactional
    public void deleteBook(Long id) {
        Book book = findBookById(id);
        bookRepository.delete(book);
    }

    @Transactional
    public BookResponse borrowBookCopy(Long id) {
        Book book = findBookById(id);
        book.borrowCopy();
        return toResponse(bookRepository.save(book));
    }

    @Transactional
    public BookResponse returnBookCopy(Long id) {
        Book book = findBookById(id);
        book.returnCopy();
        return toResponse(bookRepository.save(book));
    }

    private Book findBookById(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new BookNotFoundException("Book not found with id: " + id));
    }

    private void validateCopies(Integer totalCopies, Integer availableCopies) {
        if (totalCopies == null || totalCopies < 1) {
            throw new InvalidBookRequestException("Total copies must be at least 1");
        }

        if (availableCopies == null || availableCopies < 0) {
            throw new InvalidBookRequestException("Available copies cannot be negative");
        }

        if (availableCopies > totalCopies) {
            throw new InvalidBookRequestException("Available copies cannot exceed total copies");
        }
    }

    private BookResponse toResponse(Book book) {
        return BookResponse.builder()
                .id(book.getId())
                .isbn(book.getIsbn())
                .title(book.getTitle())
                .author(book.getAuthor())
                .description(book.getDescription())
                .genre(book.getGenre())
                .genreDisplayName(book.getGenre().getDisplayName())
                .totalCopies(book.getTotalCopies())
                .availableCopies(book.getAvailableCopies())
                .available(book.isAvailable())
                .publicationDate(book.getPublicationDate())
                .createdAt(book.getCreatedAt())
                .updatedAt(book.getUpdatedAt())
                .build();
    }
}