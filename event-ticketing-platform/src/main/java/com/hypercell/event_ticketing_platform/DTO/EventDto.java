package com.hypercell.event_ticketing_platform.DTO;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.hypercell.event_ticketing_platform.Enum.EventStatus;
import com.hypercell.event_ticketing_platform.Enum.SeatCategoryName;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * Unified Single-File DTO wrapper for Event domain operations.
 * Combines all request payloads, filter criteria, and response models.
 */
public class EventDto {

    /**
     * Request payload for creating a new event catalog entry.
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateRequest {

        @NotBlank(message = "Title is required")
        @Size(max = 255, message = "Event title must not exceed 255 characters")
        private String title;

        @Size(max = 2000, message = "Event description must not exceed 2000 characters")
        private String description;

        @NotBlank(message = "Category is required")
        @Size(max = 100, message = "Event category must not exceed 100 characters")
        private String category;

        @NotNull(message = "Start date is required")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss[.SSS][.SS][.S][XXX][X]")
        private LocalDateTime startDate;

        @NotNull(message = "End date is required")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss[.SSS][.SS][.S][XXX][X]")
        private LocalDateTime endDate;

        private EventStatus status;

        @Size(max = 150, message = "Director name must not exceed 150 characters")
        private String director;

        @Min(value = 1, message = "Duration must be at least 1 minute")
        private Integer durationMinutes;

        @Size(max = 100, message = "Language must not exceed 100 characters")
        private String language;

        private Long venueId;
        private String venueName;
        @Valid
        private List<SeatCategoryDto.CreateRequest> seatCategories;


        private String imageUrl;
    }

    /**
     * Request payload for modifying event details.
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UpdateRequest {

        @Size(max = 255, message = "Event title must not exceed 255 characters")
        private String title;

        @Size(max = 2000, message = "Event description must not exceed 2000 characters")
        private String description;

        @Size(max = 100, message = "Event category must not exceed 100 characters")
        private String category;

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss[.SSS][.SS][.S][XXX][X]")
        private LocalDateTime startDate;

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss[.SSS][.SS][.S][XXX][X]")
        private LocalDateTime endDate;

        private EventStatus status;

        @Size(max = 150, message = "Director name must not exceed 150 characters")
        private String director;

        @Min(value = 1, message = "Duration must be at least 1 minute")
        private Integer durationMinutes;

        @Size(max = 100, message = "Language must not exceed 100 characters")
        private String language;

        private Long venueId;


        private String imageUrl;
    }

    /**
     * Request payload for status transitions (PUBLISHED, CANCELLED, etc.).
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ChangeStatusRequest {

        @NotNull(message = "Event status is required")
        private EventStatus status;
    }

    /**
     * Filter payload for event search and catalog pagination.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SearchFilterRequest {

        private String category;

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        private LocalDateTime startDate;

        @Builder.Default
        private int page = 0;

        @Builder.Default
        private int size = 10;
    }

    /**
     * Summary response model for event listings.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {

        private Long id;
        private String title;
        private String description;
        private String category;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private EventStatus status;
        private String director;
        private Integer durationMinutes;
        private String language;
        private String venueName;
        private String imageUrl;
    }

    /**
     * Detailed event response model including venue details and seat category allocations.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DetailResponse {

        private Long id;
        private String title;
        private String description;
        private String category;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private EventStatus status;
        private String director;
        private Integer durationMinutes;
        private String language;
        private String imageUrl;

        private String venueName;
        private String venueAddress;

        private List<SeatCategoryResponse> seatCategories;

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class SeatCategoryResponse {
            private Long id;
            private SeatCategoryName categoryName;
            private BigDecimal price;
            private int availableSeats;
        }
    }
}
