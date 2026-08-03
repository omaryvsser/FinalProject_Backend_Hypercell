package com.hypercell.event_ticketing_platform.DTO;

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
public class VenueDto {

    private Long id; // Venue identifier

    private String name; // Venue name

    private String address; // Venue physical address

    private Integer capacity; // Maximum venue seating capacity
}
