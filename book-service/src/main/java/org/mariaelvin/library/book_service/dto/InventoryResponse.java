package org.mariaelvin.library.book_service.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class InventoryResponse {

    private Long bookId;
    private String isbn;
    private String title;
    private String author;

    private Integer totalCopies;
    private Integer availableCopies;
    private Integer borrowedCopies;

    private boolean available;
}
