package com.hypercell.event_ticketing_platform.DTO;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Unified Single-File DTO wrapper for Venue operations.
 * Contains static nested Request and Response classes to enforce 
 * clean single-file DTO architectural standards.
 */
public class VenueDto {

    /**
     * Request payload for creating a new venue.
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateRequest {

        @NotBlank(message = "Venue name is required")
        @Size(max = 150, message = "Venue name must not exceed 150 characters")
        private String name;

        @NotBlank(message = "Venue address is required")
        @Size(max = 255, message = "Venue address must not exceed 255 characters")
        private String address;

        @NotNull(message = "Venue capacity is required")
        @Min(value = 1, message = "Venue capacity must be at least 1")
        private Integer capacity;
    }

    /**
     * Request payload for updating an existing venue.
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UpdateRequest {

        @Size(max = 150, message = "Venue name must not exceed 150 characters")
        private String name;

        @Size(max = 255, message = "Venue address must not exceed 255 characters")
        private String address;

        @Min(value = 1, message = "Venue capacity must be at least 1")
        private Integer capacity;
    }

    /**
     * Response payload representing venue details.
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long id;
        private String name;
        private String address;
        private Integer capacity;
    }
}
