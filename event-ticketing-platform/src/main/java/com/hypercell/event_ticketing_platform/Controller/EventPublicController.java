package com.hypercell.event_ticketing_platform.Controller;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hypercell.event_ticketing_platform.DTO.EventDto;
import com.hypercell.event_ticketing_platform.Service.SearchPublicEvents;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/public/events")
@RequiredArgsConstructor
public class EventPublicController {

    private final SearchPublicEvents searchPublicEvents;

    @GetMapping
    public ResponseEntity<Page<EventDto.Response>> getpublicEvent(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String startDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        EventDto.SearchFilterRequest filterDto = new EventDto.SearchFilterRequest();
        filterDto.setCategory(category);

        if (startDate != null && !startDate.isEmpty()) {
            filterDto.setStartDate(java.time.LocalDateTime.parse(startDate));
        }

        filterDto.setPage(page);
        filterDto.setSize(size);

        Page<EventDto.Response> response = searchPublicEvents.searchevPage(filterDto);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventDto.DetailResponse> getEventDetails(@PathVariable Long id) {
        EventDto.DetailResponse eventDetails = searchPublicEvents.getEventDetails(id);
        return ResponseEntity.ok(eventDetails);
    }
}
