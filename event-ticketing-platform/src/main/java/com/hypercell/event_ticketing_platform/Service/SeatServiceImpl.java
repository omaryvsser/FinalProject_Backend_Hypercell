package com.hypercell.event_ticketing_platform.Service;

import com.hypercell.event_ticketing_platform.DTO.SeatCategoryDto;
import com.hypercell.event_ticketing_platform.DTO.SeatDto;
import com.hypercell.event_ticketing_platform.Entity.EventEntity;
import com.hypercell.event_ticketing_platform.Entity.SeatCategoryEntity;
import com.hypercell.event_ticketing_platform.Entity.SeatEntity;
import com.hypercell.event_ticketing_platform.Entity.VenueEntity;
import com.hypercell.event_ticketing_platform.Enum.SeatCategoryName;
import com.hypercell.event_ticketing_platform.Exception.ResourceNotFoundException;
import com.hypercell.event_ticketing_platform.Repository.EventRepository;
import com.hypercell.event_ticketing_platform.Repository.SeatCategoryRepository;
import com.hypercell.event_ticketing_platform.Repository.SeatRepository;
import com.hypercell.event_ticketing_platform.Repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SeatServiceImpl implements SeatService {

    private final SeatRepository seatRepository;
    private final SeatCategoryRepository seatCategoryRepository;
    private final EventRepository eventRepository;
    private final TicketRepository ticketRepository;

    @Override
    @Transactional
    public List<SeatDto.Response> getSeatsForEvent(Long eventId) {
        EventEntity event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + eventId));

        VenueEntity venue = event.getVenue();
        if (venue == null) {
            throw new ResourceNotFoundException("Venue not configured for event ID: " + eventId);
        }

        // 1. Fetch physical seats for venue (auto-generate layout if none exist)
        List<SeatEntity> physicalSeats = seatRepository.findByVenueIdOrderByRowNameAscSeatNumberAsc(venue.getId());
        if (physicalSeats.isEmpty()) {
            physicalSeats = generateDefaultCinemaSeatsForVenue(venue);
        }

        // 2. Fetch event seat categories to determine dynamic pricing
        List<SeatCategoryEntity> categories = seatCategoryRepository.findByEventId(eventId);
        Map<SeatCategoryName, SeatCategoryEntity> categoryMap = categories.stream()
                .collect(Collectors.toMap(SeatCategoryEntity::getName, c -> c, (c1, c2) -> c1));

        SeatCategoryEntity defaultCategory = !categories.isEmpty() ? categories.get(0) : null;

        // 3. Fetch currently booked seats for this event
        List<Long> bookedSeatIds = ticketRepository.findBookedSeatIdsByEventId(eventId);
        Set<Long> bookedSeatSet = new HashSet<>(bookedSeatIds != null ? bookedSeatIds : Collections.emptyList());

        // 4. Map to SeatDto.Response
        List<String> distinctRows = physicalSeats.stream()
                .map(SeatEntity::getRowName)
                .distinct()
                .collect(Collectors.toList());
        int totalRows = distinctRows.size();

        return physicalSeats.stream().map(seat -> {
            SeatCategoryName catName = seat.getCategoryName();
            if (catName == null) {
                int rowIndex = distinctRows.indexOf(seat.getRowName());
                if (rowIndex == totalRows - 1) {
                    catName = SeatCategoryName.IMAX;
                } else if (rowIndex == totalRows - 2) {
                    catName = SeatCategoryName.VIP;
                } else {
                    catName = SeatCategoryName.STANDARD;
                }
            }

            SeatCategoryEntity matchedCategory = categoryMap.getOrDefault(catName, defaultCategory);

            BigDecimal price = matchedCategory != null && matchedCategory.getPrice() != null
                    ? matchedCategory.getPrice()
                    : (catName == SeatCategoryName.VIP ? BigDecimal.valueOf(250.00)
                      : (catName == SeatCategoryName.IMAX ? BigDecimal.valueOf(200.00) : BigDecimal.valueOf(120.00)));

            Long seatCategoryId = matchedCategory != null ? matchedCategory.getId() : (defaultCategory != null ? defaultCategory.getId() : null);

            String status = bookedSeatSet.contains(seat.getId()) ? "BOOKED" : "AVAILABLE";

            return SeatDto.Response.builder()
                    .id(seat.getId())
                    .seatCode(seat.getSeatCode())
                    .row(seat.getRowName())
                    .number(seat.getSeatNumber())
                    .category(catName.name())
                    .price(price)
                    .seatCategoryId(seatCategoryId)
                    .status(status)
                    .build();
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public SeatDto.LayoutResponse getSeatMapLayout(Long eventId) {
        EventEntity event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + eventId));

        VenueEntity venue = event.getVenue();
        List<SeatDto.Response> seats = getSeatsForEvent(eventId);

        List<String> rows = seats.stream()
                .map(SeatDto.Response::getRow)
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        List<SeatCategoryEntity> categories = seatCategoryRepository.findByEventId(eventId);
        List<SeatCategoryDto.Response> categoryDtos = categories.stream().map(cat ->
                SeatCategoryDto.Response.builder()
                        .id(cat.getId())
                        .name(cat.getName())
                        .price(cat.getPrice())
                        .totalSeats(cat.getTotalSeats())
                        .availableSeats(cat.getAvailableSeats())
                        .eventId(cat.getEvent().getId())
                        .build()
        ).collect(Collectors.toList());

        return SeatDto.LayoutResponse.builder()
                .eventId(event.getId())
                .eventTitle(event.getTitle())
                .venueId(venue != null ? venue.getId() : null)
                .venueName(venue != null ? venue.getName() : "Cinema Hall")
                .venueCapacity(venue != null && venue.getCapacity() != null ? venue.getCapacity() : seats.size())
                .seats(seats)
                .rows(rows)
                .categories(categoryDtos)
                .build();
    }

    /**
     * Auto-generates a cinema seating layout for a venue based on venue capacity.
     * Generates exactly venue.capacity seats sequentially in rows of 10 seats.
     * Categories: Last row -> IMAX, row before last -> VIP, all earlier rows -> STANDARD.
     */
    private List<SeatEntity> generateDefaultCinemaSeatsForVenue(VenueEntity venue) {
        int capacity = (venue.getCapacity() != null && venue.getCapacity() > 0) ? venue.getCapacity() : 48;
        int seatsPerRow = 10;
        int totalRows = (capacity + seatsPerRow - 1) / seatsPerRow;

        List<SeatEntity> newSeats = new ArrayList<>();
        int generatedCount = 0;

        for (int r = 0; r < totalRows; r++) {
            String rowName = getRowName(r);

            SeatCategoryName catName;
            if (r == totalRows - 1) {
                catName = SeatCategoryName.IMAX;
            } else if (r == totalRows - 2) {
                catName = SeatCategoryName.VIP;
            } else {
                catName = SeatCategoryName.STANDARD;
            }

            int seatsInRow = Math.min(seatsPerRow, capacity - generatedCount);
            for (int num = 1; num <= seatsInRow; num++) {
                newSeats.add(SeatEntity.builder()
                        .venue(venue)
                        .rowName(rowName)
                        .seatNumber(num)
                        .seatCode(rowName + num)
                        .categoryName(catName)
                        .build());
                generatedCount++;
            }
        }

        return seatRepository.saveAll(newSeats);
    }

    private String getRowName(int index) {
        if (index < 26) {
            return String.valueOf((char) ('A' + index));
        }
        StringBuilder sb = new StringBuilder();
        while (index >= 0) {
            sb.insert(0, (char) ('A' + (index % 26)));
            index = (index / 26) - 1;
        }
        return sb.toString();
    }
}
