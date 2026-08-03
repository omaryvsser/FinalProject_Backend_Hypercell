package com.hypercell.event_ticketing_platform.DTO;

import java.math.BigDecimal;

import com.hypercell.event_ticketing_platform.Enum.SeatCategoryName;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
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
public class UpdateSeatCategoryDto {

    private SeatCategoryName name;

    @DecimalMin(value = "0.00", inclusive = true, message = "Price cannot be negative")
    private BigDecimal price;

    @Min(value = 1, message = "Total seats must be at least 1")
    private Integer totalSeats;
}
