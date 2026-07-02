package org.mariaelvin.library.book_service.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AdjustAvailableCopiesRequest {

    @NotNull(message = "Available copies is required")
    @Min(value = 0, message = "Available copies cannot be negative")
    private Integer availableCopies;
}