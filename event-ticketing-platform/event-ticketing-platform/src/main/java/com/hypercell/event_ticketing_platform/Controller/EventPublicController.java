package com.hypercell.event_ticketing_platform.Controller;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hypercell.event_ticketing_platform.DTO.EventDetailDto;
import com.hypercell.event_ticketing_platform.DTO.EventResponseDto;
import com.hypercell.event_ticketing_platform.DTO.EventSearchFilterDto;
import com.hypercell.event_ticketing_platform.Service.SearchPublicEvents;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/public/events")
@RequiredArgsConstructor
public class EventPublicController {

    private final SearchPublicEvents searchPublicEvents;

    @GetMapping
    public ResponseEntity<Page<EventResponseDto>> getpublicEvent(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String startDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        EventSearchFilterDto filterDto = new EventSearchFilterDto();
        filterDto.setCategory(category);

        if (startDate != null && !startDate.isEmpty()) {
            filterDto.setStartDate(java.time.LocalDateTime.parse(startDate));
        }

        filterDto.setPage(page);
        filterDto.setSize(size);

        Page<EventResponseDto> response = searchPublicEvents.searchevPage(filterDto);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventDetailDto> getEventDetails(@PathVariable Long id) {
        EventDetailDto eventDetails = searchPublicEvents.getEventDetails(id);
        return ResponseEntity.ok(eventDetails);
    }
}
