package org.mariaelvin.library.borrow_service.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mariaelvin.library.borrow_service.client.BookClient;
import org.mariaelvin.library.borrow_service.dto.BookResponse;
import org.mariaelvin.library.borrow_service.exception.BookServiceException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookServiceClientFacade {

    private final BookClient bookClient;

    @Retry(name = "bookServiceReadRetry", fallbackMethod = "getBookByIdFallback")
    @CircuitBreaker(name = "bookService", fallbackMethod = "getBookByIdFallback")
    public BookResponse getBookById(Long bookId) {
        return bookClient.getBookById(bookId);
    }

    @CircuitBreaker(name = "bookService", fallbackMethod = "borrowBookCopyFallback")
    public BookResponse borrowBookCopy(Long bookId) {
        return bookClient.borrowBookCopy(bookId);
    }

    @CircuitBreaker(name = "bookService", fallbackMethod = "returnBookCopyFallback")
    public BookResponse returnBookCopy(Long bookId) {
        return bookClient.returnBookCopy(bookId);
    }

    public BookResponse getBookByIdFallback(Long bookId, Throwable ex) {
        log.error("Book Service getBookById failed. bookId={}, reason={}", bookId, ex.getMessage(), ex);

        throw new BookServiceException(
                "Unable to fetch book details from Book Service. Please try again later.",
                ex
        );
    }

    public BookResponse borrowBookCopyFallback(Long bookId, Throwable ex) {
        log.error("Book Service borrowBookCopy failed. bookId={}, reason={}", bookId, ex.getMessage(), ex);

        throw new BookServiceException(
                "Unable to update book inventory for borrowing. Please try again later.",
                ex
        );
    }

    public BookResponse returnBookCopyFallback(Long bookId, Throwable ex) {
        log.error("Book Service returnBookCopy failed. bookId={}, reason={}", bookId, ex.getMessage(), ex);

        throw new BookServiceException(
                "Unable to update book inventory for return. Please try again later.",
                ex
        );
    }
}