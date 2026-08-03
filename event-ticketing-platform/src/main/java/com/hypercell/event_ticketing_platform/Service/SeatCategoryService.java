package com.hypercell.event_ticketing_platform.Service;

import com.hypercell.event_ticketing_platform.DTO.CreateSeatCategoryDto;
import com.hypercell.event_ticketing_platform.DTO.SeatCategoryResponseDto;
import com.hypercell.event_ticketing_platform.DTO.UpdateSeatCategoryDto;

import java.util.List;

public interface SeatCategoryService {

    SeatCategoryResponseDto addSeatCategory(Long eventId, CreateSeatCategoryDto createSeatCategoryDto);

    SeatCategoryResponseDto updateSeatCategory(Long eventId, Long categoryId, UpdateSeatCategoryDto updateSeatCategoryDto);

    void deleteSeatCategory(Long eventId, Long categoryId);

    List<SeatCategoryResponseDto> getSeatCategoriesByEventId(Long eventId);
}
