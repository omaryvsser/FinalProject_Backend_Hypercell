package com.hypercell.event_ticketing_platform.Service;

import com.hypercell.event_ticketing_platform.DTO.EventDto;
import com.hypercell.event_ticketing_platform.Enum.EventStatus;
import com.hypercell.event_ticketing_platform.Exception.ResourceNotFoundException;
import com.hypercell.event_ticketing_platform.Entity.EventEntity;
import com.hypercell.event_ticketing_platform.Repository.EventRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SearchPublicEvents {

    private final EventRepository eventRepository;

    public Page<EventDto.Response> searchevPage(EventDto.SearchFilterRequest filterDto) {
        Pageable pageable = PageRequest.of(filterDto.getPage(), filterDto.getSize(), Sort.by("startDate").ascending());

        Page<EventEntity> eventEntities = eventRepository.findPublicEvents(
                EventStatus.PUBLISHED,
                filterDto.getCategory(),
                filterDto.getStartDate(),
                pageable);

        return eventEntities.map(event -> EventDto.Response.builder()
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
                .build());
    }

    public EventDto.DetailResponse getEventDetails(Long eventId) {
        EventEntity event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + eventId));

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
                .seatCategories(
                        event.getSeatCategories().stream()
                                .map(seatCategory -> EventDto.DetailResponse.SeatCategoryResponse.builder()
                                        .id(seatCategory.getId())
                                        .categoryName(seatCategory.getName())
                                        .price(seatCategory.getPrice())
                                        .availableSeats(seatCategory.getAvailableSeats())
                                        .build())
                                .toList())
                .build();
    }
}