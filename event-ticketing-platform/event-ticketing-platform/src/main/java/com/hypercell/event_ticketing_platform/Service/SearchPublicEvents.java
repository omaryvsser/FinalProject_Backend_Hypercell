package com.hypercell.event_ticketing_platform.Service;

import com.hypercell.event_ticketing_platform.DTO.EventResponseDto;
import com.hypercell.event_ticketing_platform.DTO.EventSearchFilterDto;
import com.hypercell.event_ticketing_platform.Enum.EventStatus;
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

    public Page<EventResponseDto> searchevPage(EventSearchFilterDto filterDto) {
        // الـ Imports والتعديلات هنا بقت صحيحة تماماً من Spring Data
        Pageable pageable = PageRequest.of(filterDto.getPage(), filterDto.getSize(), Sort.by("startDate").ascending());

        // استدعاء الاستعلام من الداتابيز (تعديل الحروف الكابيتال والسمول)
        Page<EventEntity> eventEntities = eventRepository.findPublicEvents(
                EventStatus.PUBLISHED,
                filterDto.getCategory(),
                filterDto.getStartDate(),
                pageable);

        return eventEntities.map(event -> EventResponseDto.builder()
                .id(event.getId())
                .title(event.getTitle())
                .description(event.getDescription())
                .category(event.getCategory())
                .startDate(event.getStartDate())
                .endDate(event.getEndDate())
                .status(event.getStatus())
                .venueName(event.getVenue() != null ? event.getVenue().getName() : null)
                .build());
    }
}