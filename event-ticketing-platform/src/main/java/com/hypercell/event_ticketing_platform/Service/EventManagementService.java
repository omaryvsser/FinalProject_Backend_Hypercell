package com.hypercell.event_ticketing_platform.Service;

import com.hypercell.event_ticketing_platform.DTO.EventDto;
import org.springframework.data.domain.Page;

public interface EventManagementService {

    Page<EventDto.Response> getAllEvents(int page, int size);

    EventDto.Response createEvent(EventDto.CreateRequest createEventDto);

    EventDto.Response updateEvent(Long eventId, EventDto.UpdateRequest updateEventDto);

    EventDto.Response changeEventStatus(Long eventId, EventDto.ChangeStatusRequest statusDto);
    EventDto.DetailResponse getEventById(Long id);
    void deleteEvent(Long eventId);
}
