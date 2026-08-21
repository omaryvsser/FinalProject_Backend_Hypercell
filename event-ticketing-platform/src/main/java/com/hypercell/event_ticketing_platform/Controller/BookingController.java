package com.hypercell.event_ticketing_platform.Controller;

import com.hypercell.event_ticketing_platform.DTO.BookingDto;
import com.hypercell.event_ticketing_platform.Service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER', 'CUSTOMER')")
    public ResponseEntity<String> cancelBooking(@PathVariable Long id) {
        bookingService.cancelBooking(id);
        return ResponseEntity.ok("Booking cancelled successfully and seats restored.");
    }

    /** Updates booking status (e.g. CONFIRMED, PENDING, CANCELLED) with role-based ownership checks */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    public ResponseEntity<BookingDto.Response> updateBookingStatus(
            @PathVariable Long id,
            @Valid @RequestBody BookingDto.StatusUpdateRequest request) {
        return ResponseEntity.ok(bookingService.updateBookingStatus(id, request.getStatus()));
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

    /** Retrieves all booking records in the system (Paginated) for Admin */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<BookingDto.Response>> getAllBookings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        return ResponseEntity.ok(bookingService.getAllBookings(page, size));
    }

    /** Retrieves booking records for the currently authenticated organizer's events (Paginated) */
    @GetMapping("/organizer")
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
    public ResponseEntity<Page<BookingDto.Response>> getOrganizerBookings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        return ResponseEntity.ok(bookingService.getOrganizerBookings(page, size));
    }
}
