package org.mariaelvin.library.analytics_service.dto;

import lombok.Data;

@Data
public class BookResponse {

    private Long id;
    private String title;
    private String author;
    private String isbn;
    private String genre;
    private String genreDisplayName;

    private Integer totalCopies;
    private Integer availableCopies;
    private Boolean available;
}