package com.hypercell.event_ticketing_platform.DTO;

import com.hypercell.event_ticketing_platform.Enum.BookingStatus; // تأكد إنك عامل Enum بالحالات دي أو استخدم String لو عاملها سترينج
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingResponseDto {

    private Long bookingId;
    private String eventTitle;
    private String seatCategoryName;
    private int quantity;
    private BigDecimal totalPrice;
    private BookingStatus status;
    private LocalDateTime createdAt;
}