package org.mariaelvin.library.book_service.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class BookResponse {

    private Long id;
    private String isbn;
    private String title;
    private String author;
    private String description;
    private BookGenre genre;
    private String genreDisplayName;
    private Integer totalCopies;
    private Integer availableCopies;
    private boolean available;
    private LocalDate publicationDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}