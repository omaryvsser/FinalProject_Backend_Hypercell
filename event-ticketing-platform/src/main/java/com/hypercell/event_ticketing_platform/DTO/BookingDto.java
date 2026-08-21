package com.hypercell.event_ticketing_platform.DTO;

import com.hypercell.event_ticketing_platform.Enum.BookingStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Unified Single-File DTO wrapper for Booking operations.
 * Encapsulates static nested Request and Response classes.
 */
public class BookingDto {

    /**
     * Request payload for creating a ticket booking reservation.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateRequest {

        @NotNull(message = "Event ID is required")
        private Long eventId;

        @NotNull(message = "User ID is required")
        private Long userId;

        @NotNull(message = "Seat Category ID is required")
        private Long seatCategoryId;

        @Min(value = 1, message = "Quantity must be at least 1")
        @Max(value = 8, message = "Maximum 8 tickets allowed per booking")
        private int quantity;
    }

    /**
     * Response payload containing booking transaction details.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {

        private Long bookingId;
        private String eventTitle;
        private String seatCategoryName;
        private int quantity;
        private BigDecimal totalPrice;
        private BookingStatus status;
        private LocalDateTime createdAt;
    }
}
