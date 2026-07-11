package org.mariaelvin.library.analytics_service.client;

import org.mariaelvin.library.analytics_service.dto.BorrowRecordResponse;
import org.mariaelvin.library.analytics_service.dto.PageResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(
        name = "borrow-service",
        url = "${services.borrow-service.url}"
)
public interface BorrowServiceClient {

    @GetMapping("/api/borrow-records")
    PageResponse<BorrowRecordResponse> getAllBorrowRecords(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size
    );

    @GetMapping("/api/borrow-records/user/{userId}")
    PageResponse<BorrowRecordResponse> getBorrowRecordsByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size
    );
}