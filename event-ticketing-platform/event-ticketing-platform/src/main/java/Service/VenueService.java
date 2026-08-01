package Service;

import DTO.CreateVenueDto;
import DTO.UpdateVenueDto;
import DTO.VenueDto;

import java.util.List;

public interface VenueService {

    CreateVenueDto addVenue(CreateVenueDto venueDto);

    List<VenueDto> getAllVenues();

    VenueDto getVenueById(Long id);

    VenueDto updateVenue(Long id, UpdateVenueDto venueDto);

    void deleteVenue(Long id);
}
