package com.hypercell.event_ticketing_platform.Controller;

import com.hypercell.event_ticketing_platform.DTO.ChangeEventStatusDto;
import com.hypercell.event_ticketing_platform.DTO.CreateEventDto;
import com.hypercell.event_ticketing_platform.DTO.EventResponseDto;
import com.hypercell.event_ticketing_platform.DTO.UpdateEventDto;
import com.hypercell.event_ticketing_platform.Service.EventManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/events")
@RequiredArgsConstructor
public class EventManagementController {

    private final EventManagementService eventManagementService;

    @PostMapping
    public ResponseEntity<EventResponseDto> createEvent(@Valid @RequestBody CreateEventDto createEventDto) {
        EventResponseDto createdEvent = eventManagementService.createEvent(createEventDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdEvent);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EventResponseDto> updateEvent(
            @PathVariable Long id,
            @Valid @RequestBody UpdateEventDto updateEventDto) {
        EventResponseDto updatedEvent = eventManagementService.updateEvent(id, updateEventDto);
        return ResponseEntity.ok(updatedEvent);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<EventResponseDto> changeEventStatus(
            @PathVariable Long id,
            @Valid @RequestBody ChangeEventStatusDto statusDto) {
        EventResponseDto updatedEvent = eventManagementService.changeEventStatus(id, statusDto);
        return ResponseEntity.ok(updatedEvent);
    }
}
