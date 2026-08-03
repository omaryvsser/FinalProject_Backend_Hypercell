package com.hypercell.event_ticketing_platform.DTO;

import java.time.LocalDateTime;

import com.hypercell.event_ticketing_platform.Enum.EventStatus;
import jakarta.validation.constraints.Future;
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
public class UpdateEventDto {

    @Size(max = 255, message = "Event title must not exceed 255 characters")
    private String title; // Updated event title

    @Size(max = 2000, message = "Event description must not exceed 2000 characters")
    private String description; // Updated event description

    @Size(max = 100, message = "Event category must not exceed 100 characters")
    private String category; // Updated event category

    @Future(message = "Start date must be in the future") // Validates start date is in the future if updated
    private LocalDateTime startDate; // Updated event start date and time

    @Future(message = "End date must be in the future") // Validates end date is in the future if updated
    private LocalDateTime endDate; // Updated event end date and time

    private EventStatus status; // Updated event status

    private Long venueId; // Updated venue identifier

    @Size(max = 500, message = "Image URL must not exceed 500 characters")
    private String imageUrl; // Updated event image URL
}
