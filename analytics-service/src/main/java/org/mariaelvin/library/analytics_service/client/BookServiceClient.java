package org.mariaelvin.library.analytics_service.client;

import org.mariaelvin.library.analytics_service.dto.BookResponse;
import org.mariaelvin.library.analytics_service.dto.PageResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
        name = "book-service",
        url = "${services.book-service.url}"
)
public interface BookServiceClient {

    @GetMapping("/api/books")
    PageResponse<BookResponse> getBooks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "1") int size
    );

    @GetMapping("/api/books/available")
    PageResponse<BookResponse> getAvailableBooks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "1") int size
    );

    @GetMapping("/api/books/inventory/low-stock")
    PageResponse<BookResponse> getLowStockBooks(
            @RequestParam(defaultValue = "2") int threshold,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "1") int size
    );
}