package com.hypercell.event_ticketing_platform.Controller;

import com.hypercell.event_ticketing_platform.DTO.BookingDto;
//  [ADDED IMPORT]
import com.hypercell.event_ticketing_platform.DTO.EventOrganizerSummaryDto;
import com.hypercell.event_ticketing_platform.Service.BookingService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public ResponseEntity<BookingDto.Response> createBooking(@RequestBody BookingDto.CreateRequest request) {
        BookingDto.Response response = bookingService.createBooking(request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{bookingId}")
    public ResponseEntity<Void> cancelBooking(@PathVariable Long bookingId) {
        bookingService.cancelBooking(bookingId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<BookingDto.Response>> getUserBookings(@PathVariable Long userId) {
        List<BookingDto.Response> bookings = bookingService.getUserBookings(userId);
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/event/{eventId}")
    public ResponseEntity<List<BookingDto.Response>> getEventBookings(@PathVariable Long eventId) {
        List<BookingDto.Response> bookings = bookingService.getEventBookings(eventId);
        return ResponseEntity.ok(bookings);
    }
    //  [ADDED FOR ORGANIZER FEATURE] - Endpoint for Event Organizer Summary
    @GetMapping("/event/{eventId}/organizer-summary")
    public ResponseEntity<EventOrganizerSummaryDto> getEventOrganizerSummary(@PathVariable Long eventId) {
        EventOrganizerSummaryDto summary = bookingService.getEventOrganizerSummary(eventId);
        return ResponseEntity.ok(summary);
    }
}