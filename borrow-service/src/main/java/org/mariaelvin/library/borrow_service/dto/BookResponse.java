package org.mariaelvin.library.borrow_service.dto;

import lombok.Data;

@Data
public class BookResponse {

    private Long id;
    private String isbn;
    private String title;
    private String author;
    private Integer totalCopies;
    private Integer availableCopies;
    private boolean available;
}