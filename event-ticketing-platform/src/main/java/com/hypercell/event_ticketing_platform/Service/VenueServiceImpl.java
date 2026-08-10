package com.hypercell.event_ticketing_platform.Service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
    private final com.hypercell.event_ticketing_platform.Repository.EventRepository eventRepository;

    public VenueServiceImpl(VenueRepository venueRepository, com.hypercell.event_ticketing_platform.Repository.EventRepository eventRepository) {
        this.venueRepository = venueRepository;
        this.eventRepository = eventRepository;
    }

    @Override
    @Transactional
    public VenueDto.Response addVenue(VenueDto.CreateRequest venueDto) {
        if (venueRepository.existsByName(venueDto.getName())) {
            throw new ResourceAlreadyExistsException("Venue with name '" + venueDto.getName() + "' already exists");
        }

        VenueEntity venue = VenueEntity.builder()
                .name(venueDto.getName())
                .address(venueDto.getAddress())
                .capacity(venueDto.getCapacity())
                .build();

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
    public Page<VenueDto.Response> getPaginatedVenues(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return venueRepository.findAll(pageable).map(this::mapToVenueDto);
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
        VenueEntity venue = venueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Venue not found with id: " + id));

        java.util.List<com.hypercell.event_ticketing_platform.Entity.EventEntity> events = eventRepository.findByVenueId(id);
        if (events != null && !events.isEmpty()) {
            eventRepository.deleteAll(events);
        }

        venueRepository.delete(venue);
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
