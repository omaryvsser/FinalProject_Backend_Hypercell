package com.hypercell.event_ticketing_platform.Service;

import com.hypercell.event_ticketing_platform.DTO.EventDto;

public interface EventManagementService {

    EventDto.Response createEvent(EventDto.CreateRequest createEventDto);

    EventDto.Response updateEvent(Long eventId, EventDto.UpdateRequest updateEventDto);

    EventDto.Response changeEventStatus(Long eventId, EventDto.ChangeStatusRequest statusDto);
}
