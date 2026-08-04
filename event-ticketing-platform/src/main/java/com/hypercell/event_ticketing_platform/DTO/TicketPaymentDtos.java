package com.hypercell.event_ticketing_platform.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public class TicketPaymentDtos {

    // 1. Process Payment Request DTO
    public record ProcessPaymentDto(
            @NotNull(message = "Booking ID is required") Long bookingId,

            @NotBlank(message = "Payment method is required") String paymentMethod,

            @NotBlank(message = "Card number is required") String cardNumber) {
    }

    // 2. Payment Result Response DTO
    public record PaymentResultDto(
            Long paymentId,
            Long bookingId,
            String status,
            String message,
            LocalDateTime paymentDate) {
    }

    // 3. Ticket Response DTO
    public record TicketDto(
            Long id,
            String ticketNumber,
            String eventName,
            String seatCategoryName,
            Boolean isBooked,
            LocalDateTime bookingDate) {
    }
}