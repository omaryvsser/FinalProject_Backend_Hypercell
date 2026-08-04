package com.hypercell.event_ticketing_platform.Controller;

import com.hypercell.event_ticketing_platform.DTO.TicketPaymentDtos.TicketDto;
import com.hypercell.event_ticketing_platform.Service.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<TicketDto>> getUserTickets(@PathVariable Long userId) {
        return ResponseEntity.ok(ticketService.getUserTickets(userId));

    }
}