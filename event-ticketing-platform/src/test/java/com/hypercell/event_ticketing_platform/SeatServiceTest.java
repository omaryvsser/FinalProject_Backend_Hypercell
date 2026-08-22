package com.hypercell.event_ticketing_platform;

import com.hypercell.event_ticketing_platform.DTO.SeatDto;
import com.hypercell.event_ticketing_platform.Entity.EventEntity;
import com.hypercell.event_ticketing_platform.Entity.SeatCategoryEntity;
import com.hypercell.event_ticketing_platform.Entity.SeatEntity;
import com.hypercell.event_ticketing_platform.Entity.VenueEntity;
import com.hypercell.event_ticketing_platform.Enum.SeatCategoryName;
import com.hypercell.event_ticketing_platform.Repository.EventRepository;
import com.hypercell.event_ticketing_platform.Repository.SeatCategoryRepository;
import com.hypercell.event_ticketing_platform.Repository.SeatRepository;
import com.hypercell.event_ticketing_platform.Repository.TicketRepository;
import com.hypercell.event_ticketing_platform.Service.SeatServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SeatServiceTest {

    @Mock
    private SeatRepository seatRepository;
    @Mock
    private SeatCategoryRepository seatCategoryRepository;
    @Mock
    private EventRepository eventRepository;
    @Mock
    private TicketRepository ticketRepository;

    private SeatServiceImpl seatService;

    @BeforeEach
    void setUp() {
        seatService = new SeatServiceImpl(seatRepository, seatCategoryRepository, eventRepository, ticketRepository);
    }

    @Test
    void shouldReturnSeatsWithDynamicAvailabilityAndPricing() {
        VenueEntity venue = VenueEntity.builder().id(1L).name("Grand Cinema").capacity(100).build();
        EventEntity event = EventEntity.builder().id(5L).title("Interstellar").venue(venue).build();

        SeatCategoryEntity vipCategory = SeatCategoryEntity.builder()
                .id(10L)
                .name(SeatCategoryName.VIP)
                .price(BigDecimal.valueOf(250.00))
                .totalSeats(10)
                .availableSeats(9)
                .event(event)
                .build();

        SeatCategoryEntity standardCategory = SeatCategoryEntity.builder()
                .id(11L)
                .name(SeatCategoryName.STANDARD)
                .price(BigDecimal.valueOf(120.00))
                .totalSeats(40)
                .availableSeats(40)
                .event(event)
                .build();

        SeatEntity seatA1 = SeatEntity.builder().id(1L).rowName("A").seatNumber(1).seatCode("A1").categoryName(SeatCategoryName.STANDARD).venue(venue).build();
        SeatEntity seatE1 = SeatEntity.builder().id(2L).rowName("E").seatNumber(1).seatCode("E1").categoryName(SeatCategoryName.IMAX).venue(venue).build();
        SeatEntity seatF1 = SeatEntity.builder().id(3L).rowName("F").seatNumber(1).seatCode("F1").categoryName(SeatCategoryName.VIP).venue(venue).build();

        when(eventRepository.findById(5L)).thenReturn(Optional.of(event));
        when(seatRepository.findByVenueIdOrderByRowNameAscSeatNumberAsc(1L)).thenReturn(List.of(seatA1, seatE1, seatF1));
        when(seatCategoryRepository.findByEventId(5L)).thenReturn(List.of(vipCategory, standardCategory));
        // Seat A1 is booked
        when(ticketRepository.findBookedSeatIdsByEventId(5L)).thenReturn(List.of(1L));

        List<SeatDto.Response> seats = seatService.getSeatsForEvent(5L);

        assertEquals(3, seats.size());

        // Seat A1 (Row A -> STANDARD, Booked)
        assertEquals("A1", seats.get(0).getSeatCode());
        assertEquals("STANDARD", seats.get(0).getCategory());
        assertEquals(BigDecimal.valueOf(120.00), seats.get(0).getPrice());
        assertEquals(11L, seats.get(0).getSeatCategoryId());
        assertEquals("BOOKED", seats.get(0).getStatus());

        // Seat E1 (Row E -> IMAX, Available)
        assertEquals("E1", seats.get(1).getSeatCode());
        assertEquals("IMAX", seats.get(1).getCategory());
        assertEquals("AVAILABLE", seats.get(1).getStatus());

        // Seat F1 (Row F -> VIP, Available)
        assertEquals("F1", seats.get(2).getSeatCode());
        assertEquals("VIP", seats.get(2).getCategory());
        assertEquals(BigDecimal.valueOf(250.00), seats.get(2).getPrice());
        assertEquals(10L, seats.get(2).getSeatCategoryId());
        assertEquals("AVAILABLE", seats.get(2).getStatus());
    }

    @Test
    void shouldReturnCompleteSeatMapLayout() {
        VenueEntity venue = VenueEntity.builder().id(1L).name("Grand Cinema").capacity(100).build();
        EventEntity event = EventEntity.builder().id(5L).title("Interstellar").venue(venue).build();

        SeatCategoryEntity standardCategory = SeatCategoryEntity.builder()
                .id(11L)
                .name(SeatCategoryName.STANDARD)
                .price(BigDecimal.valueOf(120.00))
                .totalSeats(40)
                .availableSeats(40)
                .event(event)
                .build();

        SeatEntity seatA1 = SeatEntity.builder().id(1L).rowName("A").seatNumber(1).seatCode("A1").categoryName(SeatCategoryName.STANDARD).venue(venue).build();
        SeatEntity seatB1 = SeatEntity.builder().id(2L).rowName("B").seatNumber(1).seatCode("B1").categoryName(SeatCategoryName.STANDARD).venue(venue).build();

        when(eventRepository.findById(5L)).thenReturn(Optional.of(event));
        when(seatRepository.findByVenueIdOrderByRowNameAscSeatNumberAsc(1L)).thenReturn(List.of(seatA1, seatB1));
        when(seatCategoryRepository.findByEventId(5L)).thenReturn(List.of(standardCategory));
        when(ticketRepository.findBookedSeatIdsByEventId(5L)).thenReturn(List.of());

        SeatDto.LayoutResponse layout = seatService.getSeatMapLayout(5L);

        assertNotNull(layout);
        assertEquals(5L, layout.getEventId());
        assertEquals("Interstellar", layout.getEventTitle());
        assertEquals("Grand Cinema", layout.getVenueName());
        assertEquals(2, layout.getSeats().size());
        assertEquals(List.of("A", "B"), layout.getRows());
        assertEquals(1, layout.getCategories().size());
    }

    @Test
    void shouldGenerateExactly20SeatsWithDynamicCategories() {
        VenueEntity venue = VenueEntity.builder().id(10L).name("Boutique Cinema").capacity(20).build();
        EventEntity event = EventEntity.builder().id(100L).title("Indie Film").venue(venue).build();

        when(eventRepository.findById(100L)).thenReturn(Optional.of(event));
        when(seatRepository.findByVenueIdOrderByRowNameAscSeatNumberAsc(10L)).thenReturn(List.of());
        when(seatCategoryRepository.findByEventId(100L)).thenReturn(List.of());
        when(ticketRepository.findBookedSeatIdsByEventId(100L)).thenReturn(List.of());

        // Capture saved seats
        when(seatRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        List<SeatDto.Response> seats = seatService.getSeatsForEvent(100L);

        // 1. Exactly 20 seats
        assertEquals(20, seats.size());

        // 2. Rows: A (VIP), B (IMAX)
        long rowACount = seats.stream().filter(s -> "A".equals(s.getRow())).count();
        long rowBCount = seats.stream().filter(s -> "B".equals(s.getRow())).count();
        assertEquals(10, rowACount);
        assertEquals(10, rowBCount);

        // 3. Row A (2nd to last of 2 rows) -> VIP
        seats.stream().filter(s -> "A".equals(s.getRow()))
                .forEach(s -> assertEquals("VIP", s.getCategory()));

        // 4. Row B (last of 2 rows) -> IMAX
        seats.stream().filter(s -> "B".equals(s.getRow()))
                .forEach(s -> assertEquals("IMAX", s.getCategory()));

        verify(seatRepository, times(1)).saveAll(anyList());
    }

    @Test
    void shouldGenerateExactly48SeatsWithDynamicCategories() {
        VenueEntity venue = VenueEntity.builder().id(11L).name("Standard Cinema").capacity(48).build();
        EventEntity event = EventEntity.builder().id(101L).title("Blockbuster").venue(venue).build();

        when(eventRepository.findById(101L)).thenReturn(Optional.of(event));
        when(seatRepository.findByVenueIdOrderByRowNameAscSeatNumberAsc(11L)).thenReturn(List.of());
        when(seatCategoryRepository.findByEventId(101L)).thenReturn(List.of());
        when(ticketRepository.findBookedSeatIdsByEventId(101L)).thenReturn(List.of());
        when(seatRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        List<SeatDto.Response> seats = seatService.getSeatsForEvent(101L);

        // Exactly 48 seats
        assertEquals(48, seats.size());

        // 5 rows: A, B, C (STANDARD, 10 each = 30), D (VIP, 10), E (IMAX, 8)
        assertEquals(10, seats.stream().filter(s -> "A".equals(s.getRow())).count());
        assertEquals(10, seats.stream().filter(s -> "B".equals(s.getRow())).count());
        assertEquals(10, seats.stream().filter(s -> "C".equals(s.getRow())).count());
        assertEquals(10, seats.stream().filter(s -> "D".equals(s.getRow())).count());
        assertEquals(8, seats.stream().filter(s -> "E".equals(s.getRow())).count());

        seats.stream().filter(s -> List.of("A", "B", "C").contains(s.getRow()))
                .forEach(s -> assertEquals("STANDARD", s.getCategory()));
        seats.stream().filter(s -> "D".equals(s.getRow()))
                .forEach(s -> assertEquals("VIP", s.getCategory()));
        seats.stream().filter(s -> "E".equals(s.getRow()))
                .forEach(s -> assertEquals("IMAX", s.getCategory()));

        // Verify last seat code in row E is E8
        assertTrue(seats.stream().anyMatch(s -> "E8".equals(s.getSeatCode())));
        assertFalse(seats.stream().anyMatch(s -> "E9".equals(s.getSeatCode())));
    }

    @Test
    void shouldGenerateExactly57SeatsWithDynamicCategories() {
        VenueEntity venue = VenueEntity.builder().id(12L).name("Medium Cinema").capacity(57).build();
        EventEntity event = EventEntity.builder().id(102L).title("Action Movie").venue(venue).build();

        when(eventRepository.findById(102L)).thenReturn(Optional.of(event));
        when(seatRepository.findByVenueIdOrderByRowNameAscSeatNumberAsc(12L)).thenReturn(List.of());
        when(seatCategoryRepository.findByEventId(102L)).thenReturn(List.of());
        when(ticketRepository.findBookedSeatIdsByEventId(102L)).thenReturn(List.of());
        when(seatRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        List<SeatDto.Response> seats = seatService.getSeatsForEvent(102L);

        // Exactly 57 seats across 6 rows: A-D (10 each -> STANDARD), E (10 -> VIP), F (7 -> IMAX)
        assertEquals(57, seats.size());

        seats.stream().filter(s -> List.of("A", "B", "C", "D").contains(s.getRow()))
                .forEach(s -> assertEquals("STANDARD", s.getCategory()));
        seats.stream().filter(s -> "E".equals(s.getRow()))
                .forEach(s -> assertEquals("VIP", s.getCategory()));
        seats.stream().filter(s -> "F".equals(s.getRow()))
                .forEach(s -> assertEquals("IMAX", s.getCategory()));

        assertEquals(7, seats.stream().filter(s -> "F".equals(s.getRow())).count());
        assertTrue(seats.stream().anyMatch(s -> "F7".equals(s.getSeatCode())));
        assertFalse(seats.stream().anyMatch(s -> "F8".equals(s.getSeatCode())));
    }

    @Test
    void shouldGenerateExactly95SeatsWithDynamicCategories() {
        VenueEntity venue = VenueEntity.builder().id(13L).name("Large Cinema").capacity(95).build();
        EventEntity event = EventEntity.builder().id(103L).title("Sci-Fi Epic").venue(venue).build();

        when(eventRepository.findById(103L)).thenReturn(Optional.of(event));
        when(seatRepository.findByVenueIdOrderByRowNameAscSeatNumberAsc(13L)).thenReturn(List.of());
        when(seatCategoryRepository.findByEventId(103L)).thenReturn(List.of());
        when(ticketRepository.findBookedSeatIdsByEventId(103L)).thenReturn(List.of());
        when(seatRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        List<SeatDto.Response> seats = seatService.getSeatsForEvent(103L);

        // Exactly 95 seats across 10 rows: A-H (10 each -> STANDARD = 80), I (10 -> VIP), J (5 -> IMAX)
        assertEquals(95, seats.size());

        seats.stream().filter(s -> List.of("A", "B", "C", "D", "E", "F", "G", "H").contains(s.getRow()))
                .forEach(s -> assertEquals("STANDARD", s.getCategory()));
        seats.stream().filter(s -> "I".equals(s.getRow()))
                .forEach(s -> assertEquals("VIP", s.getCategory()));
        seats.stream().filter(s -> "J".equals(s.getRow()))
                .forEach(s -> assertEquals("IMAX", s.getCategory()));

        assertEquals(5, seats.stream().filter(s -> "J".equals(s.getRow())).count());
        assertTrue(seats.stream().anyMatch(s -> "J5".equals(s.getSeatCode())));
        assertFalse(seats.stream().anyMatch(s -> "J6".equals(s.getSeatCode())));
    }

    @Test
    void shouldGenerateExactly100SeatsWithDynamicCategories() {
        VenueEntity venue = VenueEntity.builder().id(14L).name("Grand Hall").capacity(100).build();
        EventEntity event = EventEntity.builder().id(104L).title("Premier Gala").venue(venue).build();

        when(eventRepository.findById(104L)).thenReturn(Optional.of(event));
        when(seatRepository.findByVenueIdOrderByRowNameAscSeatNumberAsc(14L)).thenReturn(List.of());
        when(seatCategoryRepository.findByEventId(104L)).thenReturn(List.of());
        when(ticketRepository.findBookedSeatIdsByEventId(104L)).thenReturn(List.of());
        when(seatRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        List<SeatDto.Response> seats = seatService.getSeatsForEvent(104L);

        // Exactly 100 seats across 10 rows: A-H (10 each -> STANDARD = 80), I (10 -> VIP), J (10 -> IMAX)
        assertEquals(100, seats.size());

        seats.stream().filter(s -> List.of("A", "B", "C", "D", "E", "F", "G", "H").contains(s.getRow()))
                .forEach(s -> assertEquals("STANDARD", s.getCategory()));
        seats.stream().filter(s -> "I".equals(s.getRow()))
                .forEach(s -> assertEquals("VIP", s.getCategory()));
        seats.stream().filter(s -> "J".equals(s.getRow()))
                .forEach(s -> assertEquals("IMAX", s.getCategory()));

        assertEquals(10, seats.stream().filter(s -> "J".equals(s.getRow())).count());
        assertTrue(seats.stream().anyMatch(s -> "J10".equals(s.getSeatCode())));
    }

    @Test
    void shouldBeIdempotentAndNotRegenerateIfSeatsAlreadyExist() {
        VenueEntity venue = VenueEntity.builder().id(20L).name("Existing Cinema").capacity(50).build();
        EventEntity event = EventEntity.builder().id(200L).title("Existing Event").venue(venue).build();

        SeatEntity existingSeat1 = SeatEntity.builder().id(501L).rowName("A").seatNumber(1).seatCode("A1").categoryName(SeatCategoryName.STANDARD).venue(venue).build();
        SeatEntity existingSeat2 = SeatEntity.builder().id(502L).rowName("A").seatNumber(2).seatCode("A2").categoryName(SeatCategoryName.STANDARD).venue(venue).build();

        when(eventRepository.findById(200L)).thenReturn(Optional.of(event));
        // Return existing seats
        when(seatRepository.findByVenueIdOrderByRowNameAscSeatNumberAsc(20L)).thenReturn(List.of(existingSeat1, existingSeat2));
        when(seatCategoryRepository.findByEventId(200L)).thenReturn(List.of());
        when(ticketRepository.findBookedSeatIdsByEventId(200L)).thenReturn(List.of());

        List<SeatDto.Response> seats = seatService.getSeatsForEvent(200L);

        assertEquals(2, seats.size());
        assertEquals(501L, seats.get(0).getId());
        assertEquals(502L, seats.get(1).getId());

        // Verify saveAll was NEVER called since seats already exist
        verify(seatRepository, never()).saveAll(anyList());
    }
}
