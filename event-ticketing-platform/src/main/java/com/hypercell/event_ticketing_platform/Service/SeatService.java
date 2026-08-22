package com.hypercell.event_ticketing_platform.Service;

import com.hypercell.event_ticketing_platform.DTO.SeatDto;
import java.util.List;

public interface SeatService {

    List<SeatDto.Response> getSeatsForEvent(Long eventId);

    SeatDto.LayoutResponse getSeatMapLayout(Long eventId);
}
