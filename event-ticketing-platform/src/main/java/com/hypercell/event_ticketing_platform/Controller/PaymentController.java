package com.hypercell.event_ticketing_platform.Controller;

import com.hypercell.event_ticketing_platform.DTO.TicketPaymentDtos.PaymentResultDto;
import com.hypercell.event_ticketing_platform.DTO.TicketPaymentDtos.ProcessPaymentDto;
import com.hypercell.event_ticketing_platform.Service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/process")
    public ResponseEntity<PaymentResultDto> processPayment(@Valid @RequestBody ProcessPaymentDto paymentDto) {
        PaymentResultDto result = paymentService.processPayment(paymentDto);
        return ResponseEntity.ok(result);
    }
}