package com.hypercell.event_ticketing_platform;

import com.hypercell.event_ticketing_platform.DTO.BookingDto;
import com.hypercell.event_ticketing_platform.Entity.*;
import com.hypercell.event_ticketing_platform.Enum.BookingStatus;
import com.hypercell.event_ticketing_platform.Enum.SeatCategoryName;
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

import java.math.BigDecimal;
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

    private BookingService bookingService;
    private Validator validator;

    @BeforeEach
    void setUp() {
        bookingService = new BookingService(bookingRepository, seatCategoryRepository, eventRepository, userRepository);
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
        when(seatCategoryRepository.findById(1L)).thenReturn(Optional.of(mockCategory));
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
}
