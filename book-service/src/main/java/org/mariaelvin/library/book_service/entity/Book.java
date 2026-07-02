package org.mariaelvin.library.book_service.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.mariaelvin.library.book_service.dto.BookGenre;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
        name = "books",
        indexes = {
                @Index(name = "idx_books_isbn", columnList = "isbn"),
                @Index(name = "idx_books_title", columnList = "title"),
                @Index(name = "idx_books_author", columnList = "author"),
                @Index(name = "idx_books_genre", columnList = "genre"),
                @Index(name = "idx_books_available_copies", columnList = "available_copies"),
                @Index(name = "idx_books_created_at", columnList = "created_at")
        }
)
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "ISBN is required")
    @Size(min = 10, max = 20, message = "ISBN must be between 10 and 20 characters")
    @Column(nullable = false, unique = true, length = 20)
    private String isbn;

    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title cannot exceed 200 characters")
    @Column(nullable = false, length = 200)
    private String title;

    @NotBlank(message = "Author is required")
    @Size(max = 100, message = "Author cannot exceed 100 characters")
    @Column(nullable = false, length = 100)
    private String author;

    @Column(columnDefinition = "TEXT")
    private String description;

    @NotNull(message = "Genre is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private BookGenre genre;

    @NotNull(message = "Total copies is required")
    @Min(value = 1, message = "Total copies must be at least 1")
    @Column(name = "total_copies", nullable = false)
    private Integer totalCopies = 1;

    @NotNull(message = "Available copies is required")
    @Min(value = 0, message = "Available copies cannot be negative")
    @Column(name = "available_copies", nullable = false)
    private Integer availableCopies = 1;

    @PastOrPresent(message = "Publication date cannot be in the future")
    @Column(name = "publication_date")
    private LocalDate publicationDate;

    @Column(name = "created_at", nullable = false, updatable = false, insertable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    @AssertTrue(message = "Available copies cannot exceed total copies")
    public boolean isCopiesValid() {
        if (totalCopies == null || availableCopies == null) {
            return true;
        }
        return availableCopies <= totalCopies;
    }

    public boolean isAvailable() {
        return availableCopies != null && availableCopies > 0;
    }

    public void borrowCopy() {
        if (!isAvailable()) {
            throw new IllegalStateException("Book is not available for borrowing");
        }
        availableCopies--;
    }

    public void returnCopy() {
        if (availableCopies == null || totalCopies == null) {
            throw new IllegalStateException("Book copy information is not available");
        }

        if (availableCopies >= totalCopies) {
            throw new IllegalStateException("Cannot return more copies than total copies");
        }

        availableCopies++;
    }

    public void updateDetails(String title,
                              String author,
                              String description,
                              BookGenre genre,
                              LocalDate publicationDate) {
        this.title = title;
        this.author = author;
        this.description = description;
        this.genre = genre;
        this.publicationDate = publicationDate;
    }

    public void updateCopies(Integer totalCopies, Integer availableCopies) {
        if (totalCopies == null || totalCopies < 1) {
            throw new IllegalArgumentException("Total copies must be at least 1");
        }

        if (availableCopies == null || availableCopies < 0) {
            throw new IllegalArgumentException("Available copies cannot be negative");
        }

        if (availableCopies > totalCopies) {
            throw new IllegalArgumentException("Available copies cannot exceed total copies");
        }

        this.totalCopies = totalCopies;
        this.availableCopies = availableCopies;
    }

    public Integer getBorrowedCopies() {
        if (totalCopies == null || availableCopies == null) {
            return 0;
        }
        return totalCopies - availableCopies;
    }

    public void addCopies(Integer copies) {
        if (copies == null || copies < 1) {
            throw new IllegalArgumentException("Copies must be at least 1");
        }

        this.totalCopies += copies;
        this.availableCopies += copies;
    }

    public void removeCopies(Integer copies) {
        if (copies == null || copies < 1) {
            throw new IllegalArgumentException("Copies must be at least 1");
        }

        int borrowedCopies = getBorrowedCopies();

        int newTotalCopies = this.totalCopies - copies;

        if (newTotalCopies < 1) {
            throw new IllegalArgumentException("Total copies must be at least 1");
        }

        if (newTotalCopies < borrowedCopies) {
            throw new IllegalArgumentException(
                    "Cannot remove copies because some copies are currently borrowed"
            );
        }

        this.totalCopies = newTotalCopies;

        if (this.availableCopies > newTotalCopies - borrowedCopies) {
            this.availableCopies = newTotalCopies - borrowedCopies;
        }
    }

    public void adjustAvailableCopies(Integer newAvailableCopies) {
        if (newAvailableCopies == null || newAvailableCopies < 0) {
            throw new IllegalArgumentException("Available copies cannot be negative");
        }

        if (newAvailableCopies > this.totalCopies) {
            throw new IllegalArgumentException("Available copies cannot exceed total copies");
        }

        this.availableCopies = newAvailableCopies;
    }
}