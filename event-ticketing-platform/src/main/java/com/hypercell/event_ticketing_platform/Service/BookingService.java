package com.hypercell.event_ticketing_platform.Service;

import com.hypercell.event_ticketing_platform.DTO.BookingDto;
import com.hypercell.event_ticketing_platform.Entity.*;
import com.hypercell.event_ticketing_platform.Enum.BookingStatus;
import com.hypercell.event_ticketing_platform.Enum.UserRole;
import com.hypercell.event_ticketing_platform.Repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final SeatCategoryRepository seatCategoryRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final SeatRepository seatRepository;
    private final TicketRepository ticketRepository;

    public BookingService(BookingRepository bookingRepository,
                          SeatCategoryRepository seatCategoryRepository,
                          EventRepository eventRepository,
                          UserRepository userRepository,
                          SeatRepository seatRepository,
                          TicketRepository ticketRepository) {
        this.bookingRepository = bookingRepository;
        this.seatCategoryRepository = seatCategoryRepository;
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.seatRepository = seatRepository;
        this.ticketRepository = ticketRepository;
    }

    /**
     * Creates a new booking reservation with strict Pessimistic Locking concurrency protection.
     */
    @Transactional
    public BookingDto.Response createBooking(BookingDto.CreateRequest request) {
        if (request.getQuantity() < 1) {
            throw new IllegalArgumentException("Quantity must be at least 1.");
        }

        if (request.getQuantity() > 8) {
            throw new IllegalArgumentException("Maximum 8 tickets allowed per booking.");
        }

        UserEntity user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        EventEntity event = eventRepository.findById(request.getEventId())
                .orElseThrow(() -> new RuntimeException("Event not found"));

        // 🔒 PESSIMISTIC LOCK: Acquired on SeatCategoryEntity to prevent concurrent overbooking
        SeatCategoryEntity seatCategory = seatCategoryRepository.findByIdWithLock(request.getSeatCategoryId())
                .orElseThrow(() -> new RuntimeException("Seat category not found"));

        if (seatCategory.getAvailableSeats() < request.getQuantity()) {
            throw new RuntimeException("Sorry, not enough seats available!");
        }

        List<SeatEntity> selectedSeats = new ArrayList<>();
        if (request.getSeatIds() != null && !request.getSeatIds().isEmpty()) {
            if (request.getSeatIds().size() != request.getQuantity()) {
                throw new IllegalArgumentException("Selected seat count (" + request.getSeatIds().size() +
                        ") does not match requested quantity (" + request.getQuantity() + ").");
            }

            selectedSeats = seatRepository.findByIdIn(request.getSeatIds());
            if (selectedSeats.size() != request.getSeatIds().size()) {
                throw new RuntimeException("One or more selected seats could not be found.");
            }

            // Validate that all seats belong to the event venue
            for (SeatEntity seat : selectedSeats) {
                if (event.getVenue() == null || !seat.getVenue().getId().equals(event.getVenue().getId())) {
                    throw new IllegalArgumentException("Seat " + seat.getSeatCode() + " does not belong to the event's venue.");
                }
            }

            // Validate under lock that none of the requested seats are already booked for this event
            if (ticketRepository.areAnySeatsBookedForEvent(event.getId(), request.getSeatIds())) {
                throw new IllegalStateException("One or more selected seats are no longer available. Please choose different seats.");
            }
        }

        // 1. Deduct seat capacity
        seatCategory.setAvailableSeats(seatCategory.getAvailableSeats() - request.getQuantity());
        seatCategoryRepository.save(seatCategory);

        // 2. Build Parent Booking Object
        BookingEntity booking = BookingEntity.builder()
                .user(user)
                .event(event)
                .seatCategory(seatCategory)
                .quantity(request.getQuantity())
                .status(BookingStatus.CONFIRMED)
                .bookingDate(LocalDateTime.now())
                .build();

        // 3. Generate tickets linked to physical seats or sequential indices
        if (!selectedSeats.isEmpty()) {
            for (SeatEntity seat : selectedSeats) {
                String uniqueTicketNumber = "TKN-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase() + "-" + seat.getSeatCode();
                String uniqueTicketCode = "TCK-QR-" + UUID.randomUUID().toString().toUpperCase();

                TicketEntity ticket = TicketEntity.builder()
                        .booking(booking)
                        .seat(seat)
                        .ticketNumber(uniqueTicketNumber)
                        .ticketCode(uniqueTicketCode)
                        .isBooked(true)
                        .build();

                booking.getTickets().add(ticket);
            }
        } else {
            int startingSeatIndex = seatCategory.getTotalSeats() - seatCategory.getAvailableSeats() + 1;
            for (int i = 0; i < request.getQuantity(); i++) {
                int seatNum = startingSeatIndex + i;

                String uniqueTicketNumber = "TKN-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase() + "-" + seatNum;
                String uniqueTicketCode = "TCK-QR-" + UUID.randomUUID().toString().toUpperCase();

                TicketEntity ticket = TicketEntity.builder()
                        .booking(booking)
                        .ticketNumber(uniqueTicketNumber)
                        .ticketCode(uniqueTicketCode)
                        .isBooked(true)
                        .build();

                booking.getTickets().add(ticket);
            }
        }

        // 4. Save Booking + Tickets in atomic transaction
        BookingEntity savedBooking = bookingRepository.save(booking);

        // 5. Map Response DTO
        return mapToResponse(savedBooking);
    }

    /**
     * Cancels an active booking WITHOUT acquiring a pessimistic lock.
     * Restores category capacity and marks tickets/seats as available.
     */
    @Transactional
    public void cancelBooking(Long bookingId) {
        BookingEntity booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + bookingId));

        validateBookingManagementAccess(booking);

        if (BookingStatus.CANCELLED.equals(booking.getStatus())) {
            throw new IllegalStateException("Booking is already cancelled.");
        }

        booking.setStatus(BookingStatus.CANCELLED);

        // Invalidate all associated tickets and digital passes
        if (booking.getTickets() != null) {
            for (TicketEntity ticket : booking.getTickets()) {
                ticket.setIsBooked(false);
            }
        }

        bookingRepository.save(booking);

        // Restore reserved seat capacity without pessimistic locking
        SeatCategoryEntity seatCategory = booking.getSeatCategory();
        if (seatCategory != null) {
            seatCategory.setAvailableSeats(seatCategory.getAvailableSeats() + booking.getQuantity());
            seatCategoryRepository.save(seatCategory);
        }
    }

    /**
     * Updates the status of an existing booking with role-based ownership validation.
     */
    @Transactional
    public BookingDto.Response updateBookingStatus(Long bookingId, BookingStatus newStatus) {
        if (newStatus == null) {
            throw new IllegalArgumentException("Status cannot be null.");
        }

        BookingEntity booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + bookingId));

        validateBookingManagementAccess(booking);

        if (booking.getStatus() == newStatus) {
            return mapToResponse(booking);
        }

        // If transitioning to CANCELLED, execute safe cancellation (seat restoration & ticket invalidation)
        if (newStatus == BookingStatus.CANCELLED) {
            cancelBooking(bookingId);
            return mapToResponse(bookingRepository.findById(bookingId).orElse(booking));
        }

        // A cancelled booking cannot be reactivated directly to prevent seat overbooking
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new IllegalStateException("Cancelled bookings cannot be reactivated directly.");
        }

        booking.setStatus(newStatus);
        BookingEntity saved = bookingRepository.save(booking);
        return mapToResponse(saved);
    }

    /**
     * Validates that the current authenticated user has permission to manage the given booking.
     */
    private void validateBookingManagementAccess(BookingEntity booking) {
        UserEntity currentUser = getAuthenticatedUser();

        if (currentUser.getRole() == UserRole.ADMIN) {
            return;
        }

        if (currentUser.getRole() == UserRole.ORGANIZER) {
            if (booking.getEvent() == null ||
                booking.getEvent().getOrganizer() == null ||
                !booking.getEvent().getOrganizer().getId().equals(currentUser.getId())) {
                throw new AccessDeniedException("Access denied: You can only manage bookings for your own movies/events.");
            }
            return;
        }

        if (currentUser.getRole() == UserRole.CUSTOMER) {
            if (booking.getUser() == null || !booking.getUser().getId().equals(currentUser.getId())) {
                throw new AccessDeniedException("Access denied: You can only cancel your own bookings.");
            }
            return;
        }

        throw new AccessDeniedException("Unauthorized to manage bookings.");
    }

    public List<BookingDto.Response> getUserBookings(Long userId) {
        List<BookingEntity> bookings = bookingRepository.findByUserId(userId);
        return bookings.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public List<BookingDto.Response> getEventBookings(Long eventId) {
        List<BookingEntity> bookings = bookingRepository.findByEventId(eventId);
        return bookings.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    /**
     * Retrieves all system bookings in a paginated format for Admin view.
     */
    public Page<BookingDto.Response> getAllBookings(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        return bookingRepository.findAll(pageable).map(this::mapToResponse);
    }

    /**
     * Retrieves bookings strictly belonging to the currently authenticated organizer's events.
     */
    public Page<BookingDto.Response> getOrganizerBookings(int page, int size) {
        UserEntity currentUser = getAuthenticatedUser();
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        return bookingRepository.findByOrganizerId(currentUser.getId(), pageable).map(this::mapToResponse);
    }

    private UserEntity getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("User is not authenticated");
        }
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found with email: " + email));
    }

    public BookingDto.Response mapToResponse(BookingEntity booking) {
        BookingDto.Response dto = new BookingDto.Response();
        dto.setBookingId(booking.getId());
        dto.setQuantity(booking.getQuantity());
        dto.setStatus(booking.getStatus());
        dto.setCreatedAt(booking.getBookingDate());

        if (booking.getUser() != null) {
            dto.setCustomerName(booking.getUser().getUsername());
            dto.setCustomerEmail(booking.getUser().getEmail());
        }

        if (booking.getEvent() != null) {
            dto.setEventTitle(booking.getEvent().getTitle());
            if (booking.getEvent().getOrganizer() != null) {
                dto.setOrganizerName(booking.getEvent().getOrganizer().getUsername());
            }
        }

        if (booking.getSeatCategory() != null) {
            if (booking.getSeatCategory().getName() != null) {
                dto.setSeatCategoryName(booking.getSeatCategory().getName().name());
            }
            if (booking.getSeatCategory().getPrice() != null) {
                BigDecimal totalPrice = booking.getSeatCategory().getPrice()
                        .multiply(BigDecimal.valueOf(booking.getQuantity()));
                dto.setTotalPrice(totalPrice);
            }
        }

        if (booking.getTickets() != null) {
            List<String> seatCodes = booking.getTickets().stream()
                    .filter(t -> t.getSeat() != null && t.getSeat().getSeatCode() != null)
                    .map(t -> t.getSeat().getSeatCode())
                    .sorted()
                    .collect(Collectors.toList());
            if (!seatCodes.isEmpty()) {
                dto.setSeatCodes(seatCodes);
            }
        }

        return dto;
    }
}