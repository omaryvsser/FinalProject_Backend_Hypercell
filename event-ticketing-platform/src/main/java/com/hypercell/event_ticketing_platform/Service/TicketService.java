package com.hypercell.event_ticketing_platform.Service;

import com.hypercell.event_ticketing_platform.DTO.TicketPaymentDtos.TicketDto;
import com.hypercell.event_ticketing_platform.Entity.BookingEntity;
import com.hypercell.event_ticketing_platform.Entity.TicketEntity;
import com.hypercell.event_ticketing_platform.Repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;

    // 1. توليد التذاكر بناءً على الكمية في الحجز (تستدعى تلقائياً عند اتمام الحجز)
    public void generateTicketsForBooking(BookingEntity booking) {
        List<TicketEntity> tickets = new ArrayList<>();
        int quantity = booking.getQuantity();

        for (int i = 0; i < quantity; i++) {
            TicketEntity ticket = TicketEntity.builder()
                    .ticketNumber("TICK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                    .isBooked(true)
                    .booking(booking)
                    .build();

            tickets.add(ticket);
        }

        ticketRepository.saveAll(tickets);
    }

    // 2. جلب تذاكر المستخدم وعرضها مع اسم الفعالية ومقعدها للفرونت
    public List<TicketDto> getUserTickets(Long userId) {
        List<TicketEntity> tickets = ticketRepository.findByBookingUserId(userId);

        return tickets.stream().map(ticket -> {
            BookingEntity booking = ticket.getBooking();

            String eventName = (booking.getEvent() != null) ? booking.getEvent().getTitle() : "Unknown Event";
            String seatCategoryName = (booking.getSeatCategory() != null) ? booking.getSeatCategory().getName().name()
                    : "Unknown";

            return new TicketDto(
                    ticket.getId(),
                    ticket.getTicketNumber(),
                    eventName,
                    seatCategoryName,
                    ticket.getIsBooked(),
                    booking.getBookingDate());
        }).toList();
    }
}