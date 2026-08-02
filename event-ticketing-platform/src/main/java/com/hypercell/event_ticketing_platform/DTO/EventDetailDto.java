package com.hypercell.event_ticketing_platform.DTO;

import com.hypercell.event_ticketing_platform.Enum.EventStatus;
import com.hypercell.event_ticketing_platform.Enum.SeatCategoryName;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class EventDetailDto {
    private Long id;
    private String title;
    private String description;
    private String category;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private EventStatus status;

    private String venueName;
    private String venueAddress;

    private List<SeatCategoryDto> seatCategories;

    @Data
    @Builder
    public static class SeatCategoryDto {
        private Long id;
        private SeatCategoryName categoryName;
        private BigDecimal price;
        private int availableSeats;
    }
}