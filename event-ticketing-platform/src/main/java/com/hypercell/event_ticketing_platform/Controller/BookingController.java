package com.hypercell.event_ticketing_platform.Controller;

import com.hypercell.event_ticketing_platform.DTO.BookingDto;
import com.hypercell.event_ticketing_platform.Service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller managing ticket booking creation, cancellations, and queries.
 */
@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    /** Creates a new seat booking transaction */
    @PostMapping
    public ResponseEntity<BookingDto.Response> createBooking(@Valid @RequestBody BookingDto.CreateRequest request) {
        return ResponseEntity.ok(bookingService.createBooking(request));
    }

    /** Cancels an active booking and restores seat capacity */
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<String> cancelBooking(@PathVariable Long id) {
        bookingService.cancelBooking(id);
        return ResponseEntity.ok("Booking cancelled successfully and seats restored.");
    }

    /** Retrieves all booking transactions for a user */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<BookingDto.Response>> getUserBookings(@PathVariable Long userId) {
        return ResponseEntity.ok(bookingService.getUserBookings(userId));
    }

    /** Retrieves all booking records for an event */
    @GetMapping("/event/{eventId}")
    public ResponseEntity<List<BookingDto.Response>> getEventBookings(@PathVariable Long eventId) {
        return ResponseEntity.ok(bookingService.getEventBookings(eventId));
    }
}
