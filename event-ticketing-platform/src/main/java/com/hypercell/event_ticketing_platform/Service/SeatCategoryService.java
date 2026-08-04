package com.hypercell.event_ticketing_platform.Service;

import com.hypercell.event_ticketing_platform.DTO.SeatCategoryDto;

import java.util.List;

public interface SeatCategoryService {

    SeatCategoryDto.Response addSeatCategory(Long eventId, SeatCategoryDto.CreateRequest createSeatCategoryDto);

    SeatCategoryDto.Response updateSeatCategory(Long eventId, Long categoryId, SeatCategoryDto.UpdateRequest updateSeatCategoryDto);

    void deleteSeatCategory(Long eventId, Long categoryId);

    List<SeatCategoryDto.Response> getSeatCategoriesByEventId(Long eventId);
}
