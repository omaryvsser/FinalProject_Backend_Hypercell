package com.hypercell.event_ticketing_platform.DTO;

import java.math.BigDecimal;

import com.hypercell.event_ticketing_platform.Enum.SeatCategoryName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeatCategoryResponseDto {

    private Long id;
    private SeatCategoryName name;
    private BigDecimal price;
    private Integer totalSeats;
    private Integer availableSeats;
    private Long eventId;
}
