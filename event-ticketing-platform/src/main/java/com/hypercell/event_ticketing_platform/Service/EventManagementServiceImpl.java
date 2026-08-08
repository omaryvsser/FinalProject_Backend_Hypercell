package com.hypercell.event_ticketing_platform.Service;

import com.hypercell.event_ticketing_platform.DTO.EventDto;
import com.hypercell.event_ticketing_platform.Entity.EventEntity;
import com.hypercell.event_ticketing_platform.Entity.UserEntity;
import com.hypercell.event_ticketing_platform.Entity.VenueEntity;
import com.hypercell.event_ticketing_platform.Enum.EventStatus;
import com.hypercell.event_ticketing_platform.Exception.ResourceNotFoundException;
import com.hypercell.event_ticketing_platform.Repository.EventRepository;
import com.hypercell.event_ticketing_platform.Repository.UserRepository;
import com.hypercell.event_ticketing_platform.Repository.VenueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EventManagementServiceImpl implements EventManagementService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final VenueRepository venueRepository;

    @Override
    public Page<EventDto.Response> getAllEvents(int page, int size) {
        UserEntity currentUser = getAuthenticatedUser();
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

        // If ADMIN, fetch all events. If ORGANIZER, fetch only their events:
        Page<EventEntity> eventsPage = "ADMIN".equals(currentUser.getRole().name())
                ? eventRepository.findAll(pageable)
                : eventRepository.findByOrganizerId(currentUser.getId(), pageable);

        return eventsPage.map(this::mapToEventResponseDto);
    }

    @Override
    @Transactional
    public EventDto.Response createEvent(EventDto.CreateRequest createEventDto) {
        UserEntity currentUser = getAuthenticatedUser();

        // 🟢 Dynamic Venue Resolution
        VenueEntity venue;
        if (createEventDto.getVenueName() != null && !createEventDto.getVenueName().isBlank()) {
            venue = venueRepository.findByNameIgnoreCase(createEventDto.getVenueName())
                    .orElseGet(() -> venueRepository.save(
                            VenueEntity.builder()
                                    .name(createEventDto.getVenueName())
                                    .address(createEventDto.getVenueName()) // Default fallback address
                                    .build()
                    ));
        } else if (createEventDto.getVenueId() != null) {
            venue = venueRepository.findById(createEventDto.getVenueId())
                    .orElseThrow(() -> new ResourceNotFoundException("Venue not found with id: " + createEventDto.getVenueId()));
        } else {
            // Default fallback if neither is provided
            venue = venueRepository.findById(1L)
                    .orElseThrow(() -> new ResourceNotFoundException("Default venue not found"));
        }

        EventStatus status = createEventDto.getStatus() != null ? createEventDto.getStatus() : EventStatus.DRAFT;

        EventEntity event = EventEntity.builder()
                .title(createEventDto.getTitle())
                .description(createEventDto.getDescription())
                .category(createEventDto.getCategory())
                .startDate(createEventDto.getStartDate())
                .endDate(createEventDto.getEndDate())
                .status(status)
                .organizer(currentUser)
                .venue(venue)
                .imageUrl(createEventDto.getImageUrl())
                .build();

        EventEntity savedEvent = eventRepository.save(event);
        return mapToEventResponseDto(savedEvent);
    }

    @Override
    @Transactional
    public EventDto.Response updateEvent(Long eventId, EventDto.UpdateRequest updateEventDto) {
        UserEntity currentUser = getAuthenticatedUser();

        EventEntity event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + eventId));

        verifyOwnership(event, currentUser);

        if (updateEventDto.getTitle() != null) {
            event.setTitle(updateEventDto.getTitle());
        }
        if (updateEventDto.getDescription() != null) {
            event.setDescription(updateEventDto.getDescription());
        }
        if (updateEventDto.getCategory() != null) {
            event.setCategory(updateEventDto.getCategory());
        }
        if (updateEventDto.getStartDate() != null) {
            event.setStartDate(updateEventDto.getStartDate());
        }
        if (updateEventDto.getEndDate() != null) {
            event.setEndDate(updateEventDto.getEndDate());
        }
        if (updateEventDto.getStatus() != null) {
            event.setStatus(updateEventDto.getStatus());
        }
        if (updateEventDto.getVenueId() != null) {
            VenueEntity venue = venueRepository.findById(updateEventDto.getVenueId())
                    .orElseThrow(() -> new ResourceNotFoundException("Venue not found with id: " + updateEventDto.getVenueId()));
            event.setVenue(venue);
        }
        if (updateEventDto.getImageUrl() != null) {
            event.setImageUrl(updateEventDto.getImageUrl());
        }

        EventEntity updatedEvent = eventRepository.save(event);
        return mapToEventResponseDto(updatedEvent);
    }

    @Override
    @Transactional
    public EventDto.Response changeEventStatus(Long eventId, EventDto.ChangeStatusRequest statusDto) {
        UserEntity currentUser = getAuthenticatedUser();

        EventEntity event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + eventId));

        verifyOwnership(event, currentUser);

        event.setStatus(statusDto.getStatus());
        EventEntity updatedEvent = eventRepository.save(event);
        return mapToEventResponseDto(updatedEvent);
    }

    private UserEntity getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("User is not authenticated");
        }
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found with email: " + email));
    }

    private void verifyOwnership(EventEntity event, UserEntity currentUser) {
        if (!"ADMIN".equals(currentUser.getRole().name()) && !event.getOrganizer().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You are not authorized to manage this event");
        }
    }

    private EventDto.Response mapToEventResponseDto(EventEntity event) {
        return EventDto.Response.builder()
                .id(event.getId())
                .title(event.getTitle())
                .description(event.getDescription())
                .category(event.getCategory())
                .startDate(event.getStartDate())
                .endDate(event.getEndDate())
                .status(event.getStatus())
                .venueName(event.getVenue() != null ? event.getVenue().getName() : null)
                .imageUrl(event.getImageUrl())
                .build();
    }
    private EventDto.Response mapToResponseDto(EventEntity entity) {
        return EventDto.Response.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .category(entity.getCategory())
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .status(entity.getStatus()) // 🟢 Direct EventStatus enum assignment
                .venueName(entity.getVenue() != null ? entity.getVenue().getName() : null)
                .imageUrl(entity.getImageUrl())
                .build();
    }
    @Override
    @Transactional
    public void deleteEvent(Long eventId) {
        UserEntity currentUser = getAuthenticatedUser();

        EventEntity event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + eventId));

        // Ensures only the owner (or ADMIN) can delete it
        verifyOwnership(event, currentUser);

        eventRepository.delete(event);
    }
}
