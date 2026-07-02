package org.mariaelvin.library.book_service.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateBookRequest {

    @NotBlank(message = "ISBN is required")
    @Size(min = 10, max = 20, message = "ISBN must be between 10 and 20 characters")
    private String isbn;

    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title cannot exceed 200 characters")
    private String title;

    @NotBlank(message = "Author is required")
    @Size(max = 100, message = "Author cannot exceed 100 characters")
    private String author;

    private String description;

    @NotNull(message = "Genre is required")
    private org.mariaelvin.library.book_service.dto.BookGenre genre;

    @NotNull(message = "Total copies is required")
    @Min(value = 1, message = "Total copies must be at least 1")
    private Integer totalCopies;

    @Min(value = 0, message = "Available copies cannot be negative")
    private Integer availableCopies;

    @PastOrPresent(message = "Publication date cannot be in the future")
    private LocalDate publicationDate;
}