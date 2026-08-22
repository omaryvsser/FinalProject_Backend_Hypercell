package com.hypercell.event_ticketing_platform.Controller;

import com.hypercell.event_ticketing_platform.DTO.SeatDto;
import com.hypercell.event_ticketing_platform.Service.SeatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller providing Cinema Seat Map and Seat Availability endpoints.
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SeatController {

    private final SeatService seatService;

    /**
     * Retrieves all cinema seats for an event with real-time availability and dynamic pricing.
     * GET /api/events/{eventId}/seats
     */
    @GetMapping("/events/{eventId}/seats")
    public ResponseEntity<List<SeatDto.Response>> getSeatsForEvent(@PathVariable Long eventId) {
        return ResponseEntity.ok(seatService.getSeatsForEvent(eventId));
    }

    /**
     * Retrieves the complete cinema venue layout and metadata for an event.
     * GET /api/events/{eventId}/seats/layout
     */
    @GetMapping("/events/{eventId}/seats/layout")
    public ResponseEntity<SeatDto.LayoutResponse> getSeatMapLayout(@PathVariable Long eventId) {
        return ResponseEntity.ok(seatService.getSeatMapLayout(eventId));
    }

    /**
     * Public unauthenticated access for event seat availability.
     * GET /api/public/events/{eventId}/seats
     */
    @GetMapping("/public/events/{eventId}/seats")
    public ResponseEntity<List<SeatDto.Response>> getPublicSeatsForEvent(@PathVariable Long eventId) {
        return ResponseEntity.ok(seatService.getSeatsForEvent(eventId));
    }
}
