package com.hypercell.event_ticketing_platform.DTO;

import java.math.BigDecimal;
import com.hypercell.event_ticketing_platform.Enum.SeatCategoryName;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Unified Single-File DTO wrapper for Seat Category configuration.
 * Contains static nested CreateRequest, UpdateRequest, and Response classes.
 */
public class SeatCategoryDto {

    /**
     * Request payload for creating a new seat category allocation.
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateRequest {

        @NotNull(message = "Seat category name is required")
        private SeatCategoryName name;

        @NotNull(message = "Seat price is required")
        @DecimalMin(value = "0.00", inclusive = true, message = "Price cannot be negative")
        private BigDecimal price;

        @NotNull(message = "Total seats count is required")
        @Min(value = 1, message = "Total seats must be at least 1")
        private Integer totalSeats;

        private Long eventId;
    }

    /**
     * Request payload for updating seat category pricing or capacity.
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UpdateRequest {

        private SeatCategoryName name;

        @DecimalMin(value = "0.00", inclusive = true, message = "Price cannot be negative")
        private BigDecimal price;

        @Min(value = 1, message = "Total seats must be at least 1")
        private Integer totalSeats;
    }

    /**
     * Response payload representing seat category status and availability.
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {

        private Long id;
        private SeatCategoryName name;
        private BigDecimal price;
        private Integer totalSeats;
        private Integer availableSeats;
        private Long eventId;
    }
}
