package com.hypercell.event_ticketing_platform.Controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.hypercell.event_ticketing_platform.DTO.BookingResponseDto;
import com.hypercell.event_ticketing_platform.DTO.CreateBookingRequestDto;
import com.hypercell.event_ticketing_platform.Service.BookingService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public ResponseEntity<BookingResponseDto> createBooking(@Valid @RequestBody CreateBookingRequestDto request) {
        return ResponseEntity.ok(bookingService.createBooking(request));
    }

    // بنحول بس ال حاله ال booking من pending ل cancelled
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<String> cancelBooking(@PathVariable Long id) {
        bookingService.cancelBooking(id);
        return ResponseEntity.ok("Booking cancelled successfully and seats restored.");
    }

    // عرض حجوزات مستخدم معين
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<BookingResponseDto>> getUserBookings(@PathVariable Long userId) {
        return ResponseEntity.ok(bookingService.getUserBookings(userId));
    }

    // عرض الحجوزات ايفنت معين للادمن اوو اورجنيزر
    @GetMapping("/event/{eventId}")
    public ResponseEntity<List<BookingResponseDto>> getEventBookings(@PathVariable Long eventId) {
        return ResponseEntity.ok(bookingService.getEventBookings(eventId));
    }
}
