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

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateVenueDto {

    @NotBlank(message = "Venue name is required")
    @Size(max = 150, message = "Venue name must not exceed 150 characters")
    private String name; // Venue name

    @NotBlank(message = "Venue address is required")
    @Size(max = 255, message = "Venue address must not exceed 255 characters")
    private String address; // Venue physical address

    @NotNull(message = "Venue capacity is required")
    @Min(value = 1, message = "Venue capacity must be at least 1") // Ensures venue can hold at least one attendee
    private Integer capacity; // Maximum venue seating capacity
}
