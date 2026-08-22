package com.hypercell.event_ticketing_platform.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Unified Single-File DTO wrapper for Cinema Seat Map and Seat Selection.
 */
public class SeatDto {

    /**
     * Individual Seat representation with event-specific pricing and availability status.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long id;
        private String seatCode;
        private String row;
        private Integer number;
        private String category;
        private BigDecimal price;
        private Long seatCategoryId;
        private String status; // "AVAILABLE" or "BOOKED"
    }

    /**
     * Complete Cinema Venue Layout response for an Event.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LayoutResponse {
        private Long eventId;
        private String eventTitle;
        private Long venueId;
        private String venueName;
        private Integer venueCapacity;
        private List<Response> seats;
        private List<String> rows;
        private List<SeatCategoryDto.Response> categories;
    }
}
