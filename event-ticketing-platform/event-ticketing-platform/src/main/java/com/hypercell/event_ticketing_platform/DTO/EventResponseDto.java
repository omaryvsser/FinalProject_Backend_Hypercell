package com.hypercell.event_ticketing_platform.DTO;

import java.time.LocalDateTime;

import com.hypercell.event_ticketing_platform.Enum.EventStatus;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EventResponseDto {
    private Long id;
    private String title;
    private String description;
    private String category;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private EventStatus status;
    private String venueName;
}
