package com.hypercell.event_ticketing_platform.Service;

import com.hypercell.event_ticketing_platform.DTO.EventDto;
import com.hypercell.event_ticketing_platform.DTO.SeatCategoryDto;
import com.hypercell.event_ticketing_platform.Entity.EventEntity;
import com.hypercell.event_ticketing_platform.Entity.SeatCategoryEntity;
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

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventManagementServiceImpl implements EventManagementService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final VenueRepository venueRepository;
    private final com.hypercell.event_ticketing_platform.Repository.BookingRepository bookingRepository;
    private final com.hypercell.event_ticketing_platform.Repository.TicketRepository ticketRepository;

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

        // 🟢 Dynamic Venue Resolution: Check venueId first to avoid duplicate venue inserts
        VenueEntity venue;
        if (createEventDto.getVenueId() != null) {
            venue = venueRepository.findById(createEventDto.getVenueId())
                    .orElseThrow(() -> new ResourceNotFoundException("Venue not found with id: " + createEventDto.getVenueId()));
        } else if (createEventDto.getVenueName() != null && !createEventDto.getVenueName().isBlank()) {
            venue = venueRepository.findByNameIgnoreCase(createEventDto.getVenueName().trim())
                    .orElseGet(() -> venueRepository.save(
                            VenueEntity.builder()
                                    .name(createEventDto.getVenueName().trim())
                                    .address(createEventDto.getVenueName().trim())
                                    .capacity(500)
                                    .build()
                    ));
        } else {
            venue = venueRepository.findAll().stream().findFirst()
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
                .director(createEventDto.getDirector())
                .durationMinutes(createEventDto.getDurationMinutes())
                .language(createEventDto.getLanguage())
                .organizer(currentUser)
                .venue(venue)
                .imageUrl(createEventDto.getImageUrl())
                .build();

        // 🟢 NEW: Map & Attach Seat Categories from createEventDto
        if (createEventDto.getSeatCategories() != null && !createEventDto.getSeatCategories().isEmpty()) {
            for (SeatCategoryDto.CreateRequest catDto : createEventDto.getSeatCategories()) {
                SeatCategoryEntity seatCategory = SeatCategoryEntity.builder()
                        .name(catDto.getName())
                        .price(catDto.getPrice())
                        .totalSeats(catDto.getTotalSeats())
                        .availableSeats(catDto.getTotalSeats()) // Initially available = total
                        .build();

                // Attach to event bidirectionally
                event.addSeatCategory(seatCategory);
            }
        }

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
        if (updateEventDto.getDirector() != null) {
            event.setDirector(updateEventDto.getDirector());
        }
        if (updateEventDto.getDurationMinutes() != null) {
            event.setDurationMinutes(updateEventDto.getDurationMinutes());
        }
        if (updateEventDto.getLanguage() != null) {
            event.setLanguage(updateEventDto.getLanguage());
        }
        if (updateEventDto.getVenueId() != null) {
            VenueEntity venue = venueRepository.findById(updateEventDto.getVenueId())
                    .orElseThrow(() -> new ResourceNotFoundException("Venue not found with id: " + updateEventDto.getVenueId()));
            event.setVenue(venue);
        }
        if (updateEventDto.getImageUrl() != null) {
            event.setImageUrl(updateEventDto.getImageUrl());
        }

        // 🟢 FIX: Sync / Update Seat Categories & Prices
        if (updateEventDto.getSeatCategories() != null && !updateEventDto.getSeatCategories().isEmpty()) {
            for (SeatCategoryDto.CreateRequest catDto : updateEventDto.getSeatCategories()) {
                // Find existing category by name to update price/seats, or create new if absent
                event.getSeatCategories().stream()
                        .filter(cat -> cat.getName() != null && cat.getName().equals(catDto.getName()))
                        .findFirst()
                        .ifPresentOrElse(
                                existingCat -> {
                                    existingCat.setPrice(catDto.getPrice());
                                    // Adjust total seats and set available seats accordingly
                                    int seatDiff = catDto.getTotalSeats() - existingCat.getTotalSeats();
                                    existingCat.setTotalSeats(catDto.getTotalSeats());
                                    existingCat.setAvailableSeats(Math.max(0, existingCat.getAvailableSeats() + seatDiff));
                                },
                                () -> {
                                    SeatCategoryEntity newCat = SeatCategoryEntity.builder()
                                            .name(catDto.getName())
                                            .price(catDto.getPrice())
                                            .totalSeats(catDto.getTotalSeats())
                                            .availableSeats(catDto.getTotalSeats())
                                            .build();
                                    event.addSeatCategory(newCat);
                                }
                        );
            }
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

    @Override
    @Transactional
    public void deleteEvent(Long eventId) {
        UserEntity currentUser = getAuthenticatedUser();

        EventEntity event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + eventId));

        // Ensures only the owner (or ADMIN) can delete it
        verifyOwnership(event, currentUser);

        // Clean up linked bookings and tickets for this event to avoid FK constraint violations
        var bookings = bookingRepository.findByEventId(eventId);
        if (bookings != null && !bookings.isEmpty()) {
            for (var b : bookings) {
                var tickets = ticketRepository.findByBookingUserId(b.getUser().getId());
                if (tickets != null && !tickets.isEmpty()) {
                    ticketRepository.deleteAll(tickets);
                }
            }
            bookingRepository.deleteAll(bookings);
        }

        eventRepository.delete(event);
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
                .director(event.getDirector())
                .durationMinutes(event.getDurationMinutes())
                .language(event.getLanguage())
                .venueName(event.getVenue() != null ? event.getVenue().getName() : null)
                .imageUrl(event.getImageUrl())
                .build();
    }
    @Override
    public EventDto.DetailResponse getEventById(Long id) {
        EventEntity event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + id));

        return mapToEventDetailResponseDto(event);
    }

    private EventDto.DetailResponse mapToEventDetailResponseDto(EventEntity event) {
        List<EventDto.DetailResponse.SeatCategoryResponse> categoryDtos = event.getSeatCategories().stream()
                .map(cat -> EventDto.DetailResponse.SeatCategoryResponse.builder()
                        .id(cat.getId()) // 🟢 Essential real database ID
                        .categoryName(cat.getName())
                        .price(cat.getPrice())
                        .availableSeats(cat.getAvailableSeats())
                        .build())
                .collect(Collectors.toList());

        return EventDto.DetailResponse.builder()
                .id(event.getId())
                .title(event.getTitle())
                .description(event.getDescription())
                .category(event.getCategory())
                .startDate(event.getStartDate())
                .endDate(event.getEndDate())
                .status(event.getStatus())
                .director(event.getDirector())
                .durationMinutes(event.getDurationMinutes())
                .language(event.getLanguage())
                .imageUrl(event.getImageUrl())
                .venueName(event.getVenue() != null ? event.getVenue().getName() : null)
                .venueAddress(event.getVenue() != null ? event.getVenue().getAddress() : null)
                .seatCategories(categoryDtos) // 🟢 Attached seat categories with DB IDs
                .build();
    }
}