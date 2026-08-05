package com.hypercell.event_ticketing_platform.Service;

import com.hypercell.event_ticketing_platform.DTO.TicketPaymentDtos.PaymentResultDto;
import com.hypercell.event_ticketing_platform.DTO.TicketPaymentDtos.ProcessPaymentDto;
import com.hypercell.event_ticketing_platform.Entity.BookingEntity;
import com.hypercell.event_ticketing_platform.Entity.PaymentEntity;
import com.hypercell.event_ticketing_platform.Enum.BookingStatus;
import com.hypercell.event_ticketing_platform.Enum.PaymentMethod;
import com.hypercell.event_ticketing_platform.Enum.PaymentStatus;
import com.hypercell.event_ticketing_platform.Repository.BookingRepository;
import com.hypercell.event_ticketing_platform.Repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final TicketService ticketService;

    @Transactional
    public PaymentResultDto processPayment(ProcessPaymentDto paymentDto) {
        BookingEntity booking = bookingRepository.findById(paymentDto.bookingId())
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + paymentDto.bookingId()));

        if (paymentRepository.findByBookingId(booking.getId()).isPresent()) {
            throw new RuntimeException("This booking has already been paid for.");
        }

        // Calculate total amount from seat category price × quantity
        BigDecimal totalAmount = booking.getSeatCategory().getPrice()
                .multiply(BigDecimal.valueOf(booking.getQuantity()));

        String transactionId = "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        PaymentEntity payment = PaymentEntity.builder()
                .amount(totalAmount)
                .paymentMethod(PaymentMethod.valueOf(paymentDto.paymentMethod().toUpperCase()))
                .status(PaymentStatus.SUCCESS)
                .transactionId(transactionId)
                .booking(booking)
                .build();

        paymentRepository.save(payment);

        // Confirm the booking
        booking.setStatus(BookingStatus.CONFIRMED);
        bookingRepository.save(booking);

        // Generate tickets after successful payment
        ticketService.generateTicketsForBooking(booking);

        return new PaymentResultDto(
                payment.getId(),
                booking.getId(),
                "SUCCESS",
                "Payment successful! Booking confirmed and tickets generated.",
                LocalDateTime.now());
    }
}