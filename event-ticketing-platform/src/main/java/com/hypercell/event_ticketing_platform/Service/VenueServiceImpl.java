package com.hypercell.event_ticketing_platform.Service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hypercell.event_ticketing_platform.DTO.VenueDto;
import com.hypercell.event_ticketing_platform.Entity.VenueEntity;
import com.hypercell.event_ticketing_platform.Exception.ResourceAlreadyExistsException;
import com.hypercell.event_ticketing_platform.Exception.ResourceNotFoundException;
import com.hypercell.event_ticketing_platform.Repository.VenueRepository;

@Service
public class VenueServiceImpl implements VenueService {

    private final VenueRepository venueRepository;

    public VenueServiceImpl(VenueRepository venueRepository) {
        this.venueRepository = venueRepository;
    }

    @Override
    @Transactional
    public VenueDto.Response addVenue(VenueDto.CreateRequest venueDto) {
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

        return mapToVenueDto(savedVenue);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VenueDto.Response> getAllVenues() {
        return venueRepository.findAll().stream()
                .map(this::mapToVenueDto) 
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public VenueDto.Response getVenueById(Long id) {
        VenueEntity venue = venueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Venue not found with id: " + id));

        return mapToVenueDto(venue);
    }

    @Override
    @Transactional
    public VenueDto.Response updateVenue(Long id, VenueDto.UpdateRequest venueDto) {
        VenueEntity venue = venueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Venue not found with id: " + id));

        if (venueDto.getName() != null && venueRepository.existsByNameAndIdNot(venueDto.getName(), id)) {
            throw new ResourceAlreadyExistsException("Venue with name '" + venueDto.getName() + "' already exists");
        }

        if (venueDto.getName() != null) {
            venue.setName(venueDto.getName());
        }
        if (venueDto.getAddress() != null) {
            venue.setAddress(venueDto.getAddress());
        }
        if (venueDto.getCapacity() != null) {
            venue.setCapacity(venueDto.getCapacity());
        }

        VenueEntity updatedVenue = venueRepository.save(venue);
        return mapToVenueDto(updatedVenue);
    }

    @Override
    @Transactional
    public void deleteVenue(Long id) {
        if (!venueRepository.existsById(id)) {
            throw new ResourceNotFoundException("Venue not found with id: " + id);
        }
        venueRepository.deleteById(id);
    }

    private VenueDto.Response mapToVenueDto(VenueEntity venue) {
        return VenueDto.Response.builder()
                .id(venue.getId())
                .name(venue.getName())
                .address(venue.getAddress())
                .capacity(venue.getCapacity())
                .build();
    }
}
