package Service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import DTO.CreateVenueDto;
import DTO.UpdateVenueDto;
import DTO.VenueDto;
import Entity.VenueEntity;
import Exception.ResourceAlreadyExistsException;
import Exception.ResourceNotFoundException;
import Repository.VenueRepository;

@Service
public class VenueServiceImpl implements VenueService {

    private final VenueRepository venueRepository;

    public VenueServiceImpl(VenueRepository venueRepository) {
        this.venueRepository = venueRepository;
    }

    @Override
    @Transactional
    public CreateVenueDto addVenue(CreateVenueDto venueDto) {
        // Check if a venue with the same name already exists
        if (venueRepository.existsByName(venueDto.getName())) {
            throw new ResourceAlreadyExistsException("Venue with name '" + venueDto.getName() + "' already exists");
        }

        // Convert DTO to entity manually
        VenueEntity venue = VenueEntity.builder()
                .name(venueDto.getName())
                .address(venueDto.getAddress())
                .capacity(venueDto.getCapacity())
                .build();

        // Save entity
        VenueEntity savedVenue = venueRepository.save(venue);

        // Create New Object From CreateVenueDto
        return CreateVenueDto.builder()
                .name(savedVenue.getName())
                .address(savedVenue.getAddress())
                .capacity(savedVenue.getCapacity())
                .build(); // Object Creation Finished
    }

    @Override
    @Transactional(readOnly = true)
    public List<VenueDto> getAllVenues() {
        // Fetch all venues and map each entity to DTO using Java Streams
        return venueRepository.findAll().stream() // Stream for Looping Every Venue
                .map(this::mapToVenueDto) 
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public VenueDto getVenueById(Long id) {
        // Find venue by ID or throw exception if not found
        VenueEntity venue = venueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Venue not found with id: " + id));

        return mapToVenueDto(venue);
    }

    @Override
    @Transactional
    public VenueDto updateVenue(Long id, UpdateVenueDto venueDto) {
        // Find existing venue by ID or throw exception
        VenueEntity venue = venueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Venue not found with id: " + id));

        // Check if updated name conflicts with another venue
        if (venueDto.getName() != null && venueRepository.existsByNameAndIdNot(venueDto.getName(), id)) {
            throw new ResourceAlreadyExistsException("Venue with name '" + venueDto.getName() + "' already exists");
        }

        // Update entity fields if non-null
        if (venueDto.getName() != null) {
            venue.setName(venueDto.getName());
        }
        if (venueDto.getAddress() != null) {
            venue.setAddress(venueDto.getAddress());
        }
        if (venueDto.getCapacity() != null) {
            venue.setCapacity(venueDto.getCapacity());
        }

        // Save updated entity
        VenueEntity updatedVenue = venueRepository.save(venue);

        return mapToVenueDto(updatedVenue);
    }

    @Override
    @Transactional
    public void deleteVenue(Long id) {
        // Verify venue existence before deletion
        if (!venueRepository.existsById(id)) {
            throw new ResourceNotFoundException("Venue not found with id: " + id);
        }

        // Delete venue by ID
        venueRepository.deleteById(id);
    }

    // Helper method to map VenueEntity to VenueDto manually
    private VenueDto mapToVenueDto(VenueEntity venue) {
        return VenueDto.builder()
                .id(venue.getId())
                .name(venue.getName())
                .address(venue.getAddress())
                .capacity(venue.getCapacity())
                .build();
    }
}
