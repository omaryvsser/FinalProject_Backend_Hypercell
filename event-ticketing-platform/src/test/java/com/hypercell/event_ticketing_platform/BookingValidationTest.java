package com.hypercell.event_ticketing_platform;

import com.hypercell.event_ticketing_platform.DTO.BookingDto;
import com.hypercell.event_ticketing_platform.Entity.*;
import com.hypercell.event_ticketing_platform.Enum.BookingStatus;
import com.hypercell.event_ticketing_platform.Enum.SeatCategoryName;
import com.hypercell.event_ticketing_platform.Enum.UserRole;
import com.hypercell.event_ticketing_platform.Repository.*;
import com.hypercell.event_ticketing_platform.Service.BookingService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingValidationTest {

    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private SeatCategoryRepository seatCategoryRepository;
    @Mock
    private EventRepository eventRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private SeatRepository seatRepository;
    @Mock
    private TicketRepository ticketRepository;

    private BookingService bookingService;
    private Validator validator;

    @BeforeEach
    void setUp() {
        bookingService = new BookingService(
                bookingRepository,
                seatCategoryRepository,
                eventRepository,
                userRepository,
                seatRepository,
                ticketRepository
        );
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void shouldFailBeanValidationWhenQuantityExceeds8() {
        BookingDto.CreateRequest request = BookingDto.CreateRequest.builder()
                .eventId(1L)
                .userId(1L)
                .seatCategoryId(1L)
                .quantity(9)
                .build();

        Set<ConstraintViolation<BookingDto.CreateRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty(), "Violations should not be empty for quantity > 8");
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Maximum 8 tickets allowed per booking")));
    }

    @Test
    void shouldPassBeanValidationWhenQuantityIsBetween1And8() {
        for (int q = 1; q <= 8; q++) {
            BookingDto.CreateRequest request = BookingDto.CreateRequest.builder()
                    .eventId(1L)
                    .userId(1L)
                    .seatCategoryId(1L)
                    .quantity(q)
                    .build();

            Set<ConstraintViolation<BookingDto.CreateRequest>> violations = validator.validate(request);
            assertTrue(violations.isEmpty(), "Violations should be empty for valid quantity: " + q);
        }
    }

    @Test
    void shouldThrowExceptionInServiceWhenQuantityExceeds8() {
        BookingDto.CreateRequest request = BookingDto.CreateRequest.builder()
                .eventId(1L)
                .userId(1L)
                .seatCategoryId(1L)
                .quantity(9)
                .build();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            bookingService.createBooking(request);
        });

        assertEquals("Maximum 8 tickets allowed per booking.", ex.getMessage());
        verifyNoInteractions(seatCategoryRepository);
        verifyNoInteractions(bookingRepository);
    }

    @Test
    void shouldThrowExceptionInServiceWhenQuantityIsLessThan1() {
        BookingDto.CreateRequest request = BookingDto.CreateRequest.builder()
                .eventId(1L)
                .userId(1L)
                .seatCategoryId(1L)
                .quantity(0)
                .build();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            bookingService.createBooking(request);
        });

        assertEquals("Quantity must be at least 1.", ex.getMessage());
        verifyNoInteractions(seatCategoryRepository);
        verifyNoInteractions(bookingRepository);
    }

    @Test
    void shouldSuccessfullyCreateBookingWhenQuantityIs8() {
        UserEntity mockUser = UserEntity.builder().id(1L).email("user@example.com").build();
        EventEntity mockEvent = EventEntity.builder().id(1L).title("Dune").build();
        SeatCategoryEntity mockCategory = SeatCategoryEntity.builder()
                .id(1L)
                .name(SeatCategoryName.STANDARD)
                .totalSeats(50)
                .availableSeats(20)
                .price(BigDecimal.valueOf(100))
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(mockEvent));
        // Verify pessimistic lock method findByIdWithLock is called
        when(seatCategoryRepository.findByIdWithLock(1L)).thenReturn(Optional.of(mockCategory));
        when(bookingRepository.save(any(BookingEntity.class))).thenAnswer(invocation -> {
            BookingEntity b = invocation.getArgument(0);
            b.setId(999L);
            return b;
        });

        BookingDto.CreateRequest request = BookingDto.CreateRequest.builder()
                .eventId(1L)
                .userId(1L)
                .seatCategoryId(1L)
                .quantity(8)
                .build();

        BookingDto.Response response = bookingService.createBooking(request);

        assertNotNull(response);
        assertEquals(8, response.getQuantity());
        assertEquals(BookingStatus.CONFIRMED, response.getStatus());
        assertEquals(12, mockCategory.getAvailableSeats()); // 20 - 8 = 12
        verify(seatCategoryRepository).save(mockCategory);
        verify(bookingRepository).save(any(BookingEntity.class));
    }

    @Test
    void shouldSuccessfullyBookSpecificSeatsUnderLock() {
        VenueEntity mockVenue = VenueEntity.builder().id(10L).name("Cinema 1").build();
        UserEntity mockUser = UserEntity.builder().id(1L).email("user@example.com").build();
        EventEntity mockEvent = EventEntity.builder().id(1L).title("Oppenheimer").venue(mockVenue).build();
        SeatCategoryEntity mockCategory = SeatCategoryEntity.builder()
                .id(1L)
                .name(SeatCategoryName.VIP)
                .totalSeats(20)
                .availableSeats(10)
                .price(BigDecimal.valueOf(200))
                .build();

        SeatEntity seatA1 = SeatEntity.builder().id(101L).seatCode("A1").rowName("A").seatNumber(1).venue(mockVenue).build();
        SeatEntity seatA2 = SeatEntity.builder().id(102L).seatCode("A2").rowName("A").seatNumber(2).venue(mockVenue).build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(mockEvent));
        when(seatCategoryRepository.findByIdWithLock(1L)).thenReturn(Optional.of(mockCategory));
        when(seatRepository.findByIdIn(List.of(101L, 102L))).thenReturn(List.of(seatA1, seatA2));
        when(ticketRepository.areAnySeatsBookedForEvent(1L, List.of(101L, 102L))).thenReturn(false);
        when(bookingRepository.save(any(BookingEntity.class))).thenAnswer(invocation -> {
            BookingEntity b = invocation.getArgument(0);
            b.setId(888L);
            return b;
        });

        BookingDto.CreateRequest request = BookingDto.CreateRequest.builder()
                .eventId(1L)
                .userId(1L)
                .seatCategoryId(1L)
                .quantity(2)
                .seatIds(List.of(101L, 102L))
                .build();

        BookingDto.Response response = bookingService.createBooking(request);

        assertNotNull(response);
        assertEquals(2, response.getQuantity());
        assertEquals(8, mockCategory.getAvailableSeats());
        assertEquals(List.of("A1", "A2"), response.getSeatCodes());
        verify(seatCategoryRepository).findByIdWithLock(1L);
        verify(ticketRepository).areAnySeatsBookedForEvent(1L, List.of(101L, 102L));
        verify(bookingRepository).save(any(BookingEntity.class));
    }

    @Test
    void shouldFailWhenSelectedSeatIsAlreadyBooked() {
        VenueEntity mockVenue = VenueEntity.builder().id(10L).name("Cinema 1").build();
        UserEntity mockUser = UserEntity.builder().id(1L).email("user@example.com").build();
        EventEntity mockEvent = EventEntity.builder().id(1L).title("Oppenheimer").venue(mockVenue).build();
        SeatCategoryEntity mockCategory = SeatCategoryEntity.builder()
                .id(1L)
                .name(SeatCategoryName.VIP)
                .totalSeats(20)
                .availableSeats(10)
                .price(BigDecimal.valueOf(200))
                .build();

        SeatEntity seatA1 = SeatEntity.builder().id(101L).seatCode("A1").venue(mockVenue).build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(mockEvent));
        when(seatCategoryRepository.findByIdWithLock(1L)).thenReturn(Optional.of(mockCategory));
        when(seatRepository.findByIdIn(List.of(101L))).thenReturn(List.of(seatA1));
        when(ticketRepository.areAnySeatsBookedForEvent(1L, List.of(101L))).thenReturn(true);

        BookingDto.CreateRequest request = BookingDto.CreateRequest.builder()
                .eventId(1L)
                .userId(1L)
                .seatCategoryId(1L)
                .quantity(1)
                .seatIds(List.of(101L))
                .build();

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
            bookingService.createBooking(request);
        });

        assertTrue(ex.getMessage().contains("no longer available"));
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void shouldCancelBookingWithoutPessimisticLockAndFreeSeats() {
        UserEntity customer = UserEntity.builder().id(5L).email("customer@example.com").role(UserRole.CUSTOMER).build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("customer@example.com", null,
                        List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_CUSTOMER")))
        );
        when(userRepository.findByEmail("customer@example.com")).thenReturn(Optional.of(customer));

        SeatCategoryEntity mockCategory = SeatCategoryEntity.builder()
                .id(1L)
                .name(SeatCategoryName.STANDARD)
                .totalSeats(50)
                .availableSeats(18)
                .build();

        TicketEntity ticket1 = TicketEntity.builder().id(1L).isBooked(true).build();
        TicketEntity ticket2 = TicketEntity.builder().id(2L).isBooked(true).build();

        BookingEntity mockBooking = BookingEntity.builder()
                .id(77L)
                .user(customer)
                .status(BookingStatus.CONFIRMED)
                .quantity(2)
                .seatCategory(mockCategory)
                .bookingDate(LocalDateTime.now())
                .build();
        mockBooking.addTicket(ticket1);
        mockBooking.addTicket(ticket2);

        when(bookingRepository.findById(77L)).thenReturn(Optional.of(mockBooking));

        bookingService.cancelBooking(77L);

        assertEquals(BookingStatus.CANCELLED, mockBooking.getStatus());
        assertFalse(ticket1.getIsBooked());
        assertFalse(ticket2.getIsBooked());
        assertEquals(20, mockCategory.getAvailableSeats()); // 18 + 2 = 20
        verify(seatCategoryRepository).save(mockCategory);
        // Verify findByIdWithLock was NOT called during cancellation
        verify(seatCategoryRepository, never()).findByIdWithLock(any());
    }
}
