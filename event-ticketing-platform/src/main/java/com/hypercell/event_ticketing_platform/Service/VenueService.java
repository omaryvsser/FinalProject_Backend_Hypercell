package com.hypercell.event_ticketing_platform.Service;

import com.hypercell.event_ticketing_platform.DTO.VenueDto;

import java.util.List;

public interface VenueService {

    VenueDto.Response addVenue(VenueDto.CreateRequest venueDto);

    List<VenueDto.Response> getAllVenues();

    VenueDto.Response getVenueById(Long id);

    VenueDto.Response updateVenue(Long id, VenueDto.UpdateRequest venueDto);

    void deleteVenue(Long id);
}
