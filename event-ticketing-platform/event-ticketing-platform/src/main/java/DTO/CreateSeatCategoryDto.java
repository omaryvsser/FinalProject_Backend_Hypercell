package DTO;

import java.math.BigDecimal;

import Enum.SeatCategoryName;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
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
public class CreateSeatCategoryDto {

    @NotNull(message = "Seat category name is required")
    private SeatCategoryName name; // Seat category type (e.g., VIP, STANDARD, IMAX)

    @NotNull(message = "Seat price is required")
    @DecimalMin(value = "0.00", inclusive = true, message = "Price cannot be negative") // Price must be non-negative
    private BigDecimal price; // Ticket price for this seat category

    @NotNull(message = "Total seats count is required")
    @Min(value = 1, message = "Total seats must be at least 1") // Category must have at least one seat allocated
    private Integer totalSeats; // Total number of seats allocated for this category

    private Long eventId; // Associated event identifier
}
