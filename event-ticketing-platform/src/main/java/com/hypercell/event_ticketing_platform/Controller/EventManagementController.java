package com.hypercell.event_ticketing_platform.Controller;

import com.hypercell.event_ticketing_platform.DTO.EventDto;
import com.hypercell.event_ticketing_platform.Service.EventManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/events")
@RequiredArgsConstructor
public class EventManagementController {

    private final EventManagementService eventManagementService;

    @GetMapping
    public ResponseEntity<Page<EventDto.Response>> getAllEvents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size) {

        Page<EventDto.Response> events = eventManagementService.getAllEvents(page, size);
        return ResponseEntity.ok(events);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
    public ResponseEntity<EventDto.Response> createEvent(@Valid @RequestBody EventDto.CreateRequest createEventDto) {
        EventDto.Response createdEvent = eventManagementService.createEvent(createEventDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdEvent);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
    public ResponseEntity<EventDto.Response> updateEvent(
            @PathVariable Long id,
            @Valid @RequestBody EventDto.UpdateRequest updateEventDto) {
        EventDto.Response updatedEvent = eventManagementService.updateEvent(id, updateEventDto);
        return ResponseEntity.ok(updatedEvent);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
    public ResponseEntity<EventDto.Response> changeEventStatus(
            @PathVariable Long id,
            @Valid @RequestBody EventDto.ChangeStatusRequest statusDto) {
        EventDto.Response updatedEvent = eventManagementService.changeEventStatus(id, statusDto);
        return ResponseEntity.ok(updatedEvent);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long id) {
        eventManagementService.deleteEvent(id);
        return ResponseEntity.noContent().build();
    }
    @GetMapping("/{id}")
    public ResponseEntity<EventDto.DetailResponse> getEventById(@PathVariable Long id) {
        EventDto.DetailResponse event = eventManagementService.getEventById(id);
        return ResponseEntity.ok(event);
    }
}
