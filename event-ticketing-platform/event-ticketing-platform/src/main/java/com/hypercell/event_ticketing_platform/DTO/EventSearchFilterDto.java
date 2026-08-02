package com.hypercell.event_ticketing_platform.DTO;

import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;

import lombok.Data;

@Data

public class EventSearchFilterDto
// ده اللي بيستقبل الفلاتر{ اللي المستخدم هيدخلها
{
    private String category;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)

    private LocalDateTime startDate;

    private int page = 0;
    private int size = 10;

}
