package com.hypercell.event_ticketing_platform.Service;

import com.hypercell.event_ticketing_platform.DTO.ChangeEventStatusDto;
import com.hypercell.event_ticketing_platform.DTO.CreateEventDto;
import com.hypercell.event_ticketing_platform.DTO.EventResponseDto;
import com.hypercell.event_ticketing_platform.DTO.UpdateEventDto;

public interface EventManagementService {

    EventResponseDto createEvent(CreateEventDto createEventDto);

    EventResponseDto updateEvent(Long eventId, UpdateEventDto updateEventDto);

    EventResponseDto changeEventStatus(Long eventId, ChangeEventStatusDto statusDto);
}
