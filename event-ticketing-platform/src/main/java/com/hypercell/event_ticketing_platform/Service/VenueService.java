package com.hypercell.event_ticketing_platform.Service;

import com.hypercell.event_ticketing_platform.DTO.VenueDto;
import org.springframework.data.domain.Page;

import java.util.List;

public interface VenueService {

    VenueDto.Response addVenue(VenueDto.CreateRequest venueDto);

    List<VenueDto.Response> getAllVenues();

    Page<VenueDto.Response> getPaginatedVenues(int page, int size);

    VenueDto.Response getVenueById(Long id);

    VenueDto.Response updateVenue(Long id, VenueDto.UpdateRequest venueDto);

    void deleteVenue(Long id);
}
