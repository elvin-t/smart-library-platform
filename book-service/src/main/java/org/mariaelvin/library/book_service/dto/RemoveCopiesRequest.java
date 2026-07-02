package org.mariaelvin.library.book_service.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RemoveCopiesRequest {

    @NotNull(message = "Copies count is required")
    @Min(value = 1, message = "Copies count must be at least 1")
    private Integer copies;
}