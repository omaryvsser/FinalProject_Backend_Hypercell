package com.hypercell.event_ticketing_platform.Controller;

import com.hypercell.event_ticketing_platform.DTO.EventDto;
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
    public ResponseEntity<EventDto.Response> createEvent(@Valid @RequestBody EventDto.CreateRequest createEventDto) {
        EventDto.Response createdEvent = eventManagementService.createEvent(createEventDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdEvent);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EventDto.Response> updateEvent(
            @PathVariable Long id,
            @Valid @RequestBody EventDto.UpdateRequest updateEventDto) {
        EventDto.Response updatedEvent = eventManagementService.updateEvent(id, updateEventDto);
        return ResponseEntity.ok(updatedEvent);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<EventDto.Response> changeEventStatus(
            @PathVariable Long id,
            @Valid @RequestBody EventDto.ChangeStatusRequest statusDto) {
        EventDto.Response updatedEvent = eventManagementService.changeEventStatus(id, statusDto);
        return ResponseEntity.ok(updatedEvent);
    }
}
