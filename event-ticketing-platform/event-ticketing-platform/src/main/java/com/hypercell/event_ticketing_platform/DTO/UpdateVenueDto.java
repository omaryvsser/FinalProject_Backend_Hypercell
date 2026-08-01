package com.hypercell.event_ticketing_platform.DTO;

import jakarta.validation.constraints.Min;
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
public class UpdateVenueDto {

    @Size(max = 150, message = "Venue name must not exceed 150 characters")
    private String name; // Updated venue name

    @Size(max = 255, message = "Venue address must not exceed 255 characters")
    private String address; // Updated venue physical address

    @Min(value = 1, message = "Venue capacity must be at least 1")
    private Integer capacity; // Updated venue seating capacity
}
