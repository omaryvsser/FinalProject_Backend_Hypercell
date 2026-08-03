package com.hypercell.event_ticketing_platform.DTO;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.hypercell.event_ticketing_platform.Enum.EventStatus;
import jakarta.validation.constraints.FutureOrPresent;
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

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss[.SSS][.SS][.S][XXX][X]")
    private LocalDateTime startDate; // Updated event start date and time

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss[.SSS][.SS][.S][XXX][X]")
    private LocalDateTime endDate; // Updated event end date and time

    private EventStatus status; // Updated event status

    private Long venueId; // Updated venue identifier

    @Size(max = 500, message = "Image URL must not exceed 500 characters")
    private String imageUrl; // Updated event image URL
}
