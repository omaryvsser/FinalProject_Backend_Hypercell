package com.hypercell.event_ticketing_platform.Service;

import com.hypercell.event_ticketing_platform.DTO.TicketPaymentDtos.TicketDto;
import com.hypercell.event_ticketing_platform.Entity.BookingEntity;
import com.hypercell.event_ticketing_platform.Entity.TicketEntity;
import com.hypercell.event_ticketing_platform.Repository.TicketRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;

    // 1. توليد التذاكر بناءً على الكمية في الحجز (تستدعى تلقائياً عند اتمام الحجز)
    public void generateTicketsForBooking(BookingEntity booking) {
        List<TicketEntity> tickets = new ArrayList<>();
        int quantity = booking.getQuantity();

        for (int i = 0; i < quantity; i++) {
            String ticketNum = "TICK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            String ticketCode = "TCK-QR-" + UUID.randomUUID().toString().toUpperCase();

            TicketEntity ticket = TicketEntity.builder()
                    .ticketNumber(ticketNum)
                    .ticketCode(ticketCode)
                    .isBooked(true)
                    .booking(booking)
                    .build();

            tickets.add(ticket);
        }

        ticketRepository.saveAll(tickets);
    }

    // 2. Fetch user tickets with QR ticket codes
    public List<TicketDto> getUserTickets(Long userId) {
        List<TicketEntity> tickets = ticketRepository.findByBookingUserId(userId);

        return tickets.stream().map(ticket -> {
            BookingEntity booking = ticket.getBooking();

            String eventName = (booking.getEvent() != null) ? booking.getEvent().getTitle() : "Unknown Event";
            String seatCategoryName = (booking.getSeatCategory() != null) ? booking.getSeatCategory().getName().name()
                    : "Unknown";

            String code = ticket.getTicketCode() != null && !ticket.getTicketCode().isEmpty()
                    ? ticket.getTicketCode()
                    : (ticket.getTicketNumber() != null ? ticket.getTicketNumber() : "TCK-QR-" + ticket.getId());

            return new TicketDto(
                    ticket.getId(),
                    ticket.getTicketNumber(),
                    code,
                    eventName,
                    seatCategoryName,
                    ticket.getIsBooked(),
                    booking.getBookingDate());
        }).toList();
    }
}