package org.mariaelvin.library.borrow_service.client;

import org.mariaelvin.library.borrow_service.config.FeignClientConfig;
import org.mariaelvin.library.borrow_service.dto.BookResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(
        name = "book-service",
        url = "${services.book-service.url}",
        configuration = FeignClientConfig.class
)
public interface BookClient {

    @GetMapping("/api/books/{id}")
    BookResponse getBookById(@PathVariable Long id);

    @PostMapping("/api/books/{id}/borrow")
    BookResponse borrowBookCopy(@PathVariable Long id);

    @PostMapping("/api/books/{id}/return")
    BookResponse returnBookCopy(@PathVariable Long id);
}