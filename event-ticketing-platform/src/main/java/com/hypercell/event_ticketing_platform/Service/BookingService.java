package com.hypercell.event_ticketing_platform.Service;

import com.hypercell.event_ticketing_platform.DTO.BookingDto;
import com.hypercell.event_ticketing_platform.Entity.BookingEntity;
import com.hypercell.event_ticketing_platform.Entity.EventEntity;
import com.hypercell.event_ticketing_platform.Entity.SeatCategoryEntity;
import com.hypercell.event_ticketing_platform.Entity.UserEntity;
import com.hypercell.event_ticketing_platform.Enum.BookingStatus;
import com.hypercell.event_ticketing_platform.Repository.BookingRepository;
import com.hypercell.event_ticketing_platform.Repository.EventRepository;
import com.hypercell.event_ticketing_platform.Repository.SeatCategoryRepository;
import com.hypercell.event_ticketing_platform.Repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final SeatCategoryRepository seatCategoryRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    public BookingService(BookingRepository bookingRepository,
            SeatCategoryRepository seatCategoryRepository,
            EventRepository eventRepository,
            UserRepository userRepository) {
        this.bookingRepository = bookingRepository;
        this.seatCategoryRepository = seatCategoryRepository;
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public BookingDto.Response createBooking(BookingDto.CreateRequest request) {
        UserEntity user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        EventEntity event = eventRepository.findById(request.getEventId())
                .orElseThrow(() -> new RuntimeException("Event not found"));

        SeatCategoryEntity seatCategory = seatCategoryRepository.findById(request.getSeatCategoryId())
                .orElseThrow(() -> new RuntimeException("Seat category not found"));

        if (seatCategory.getAvailableSeats() < request.getQuantity()) {
            throw new RuntimeException("Sorry, not enough seats available!");
        }

        seatCategory.setAvailableSeats(seatCategory.getAvailableSeats() - request.getQuantity());
        seatCategoryRepository.save(seatCategory);

        BookingEntity booking = BookingEntity.builder()
                .user(user)
                .event(event)
                .seatCategory(seatCategory)
                .quantity(request.getQuantity())
                .status(BookingStatus.PENDING)
                .bookingDate(LocalDateTime.now())
                .build();

        BookingEntity savedBooking = bookingRepository.save(booking);

        BookingDto.Response dto = new BookingDto.Response();
        dto.setBookingId(savedBooking.getId());
        dto.setQuantity(savedBooking.getQuantity());
        dto.setStatus(savedBooking.getStatus());
        dto.setCreatedAt(savedBooking.getBookingDate());

        if (savedBooking.getEvent() != null) {
            dto.setEventTitle(savedBooking.getEvent().getTitle());
        }

        if (savedBooking.getSeatCategory() != null) {
            dto.setSeatCategoryName(savedBooking.getSeatCategory().getName().name());
            if (savedBooking.getSeatCategory().getPrice() != null) {
                BigDecimal totalPrice = savedBooking.getSeatCategory().getPrice()
                        .multiply(BigDecimal.valueOf(savedBooking.getQuantity()));
                dto.setTotalPrice(totalPrice);
            }
        }

        return dto;
    }

    @Transactional
    public void cancelBooking(Long bookingId) {
        BookingEntity booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (BookingStatus.CANCELLED.equals(booking.getStatus())) {
            throw new RuntimeException("Booking is already cancelled.");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);

        SeatCategoryEntity seatCategory = booking.getSeatCategory();
        seatCategory.setAvailableSeats(seatCategory.getAvailableSeats() + booking.getQuantity());
        seatCategoryRepository.save(seatCategory);
    }

    public List<BookingDto.Response> getUserBookings(Long userId) {
        List<BookingEntity> bookings = bookingRepository.findByUserId(userId);

        return bookings.stream().map(booking -> {
            BookingDto.Response dto = new BookingDto.Response();
            dto.setBookingId(booking.getId());
            dto.setQuantity(booking.getQuantity());
            dto.setStatus(booking.getStatus());
            dto.setCreatedAt(booking.getBookingDate());

            if (booking.getEvent() != null) {
                dto.setEventTitle(booking.getEvent().getTitle());
            }

            if (booking.getSeatCategory() != null) {
                dto.setSeatCategoryName(booking.getSeatCategory().getName().name());
                if (booking.getSeatCategory().getPrice() != null) {
                    BigDecimal totalPrice = booking.getSeatCategory().getPrice()
                            .multiply(BigDecimal.valueOf(booking.getQuantity()));
                    dto.setTotalPrice(totalPrice);
                }
            }
            return dto;
        }).collect(Collectors.toList());
    }

    public List<BookingDto.Response> getEventBookings(Long eventId) {
        List<BookingEntity> bookings = bookingRepository.findByEventId(eventId);

        return bookings.stream().map(booking -> {
            BookingDto.Response dto = new BookingDto.Response();
            dto.setBookingId(booking.getId());
            dto.setQuantity(booking.getQuantity());
            dto.setStatus(booking.getStatus());
            dto.setCreatedAt(booking.getBookingDate());

            if (booking.getEvent() != null) {
                dto.setEventTitle(booking.getEvent().getTitle());
            }

            if (booking.getSeatCategory() != null) {
                dto.setSeatCategoryName(booking.getSeatCategory().getName().name());
                if (booking.getSeatCategory().getPrice() != null) {
                    BigDecimal totalPrice = booking.getSeatCategory().getPrice()
                            .multiply(BigDecimal.valueOf(booking.getQuantity()));
                    dto.setTotalPrice(totalPrice);
                }
            }
            return dto;
        }).collect(Collectors.toList());
    }
}
