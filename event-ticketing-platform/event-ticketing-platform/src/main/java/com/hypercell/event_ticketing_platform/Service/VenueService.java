package com.hypercell.event_ticketing_platform.Service;

import com.hypercell.event_ticketing_platform.DTO.CreateVenueDto;
import com.hypercell.event_ticketing_platform.DTO.UpdateVenueDto;
import com.hypercell.event_ticketing_platform.DTO.VenueDto;

import java.util.List;

public interface VenueService {

    CreateVenueDto addVenue(CreateVenueDto venueDto);

    List<VenueDto> getAllVenues();

    VenueDto getVenueById(Long id);

    VenueDto updateVenue(Long id, UpdateVenueDto venueDto);

    void deleteVenue(Long id);
}
