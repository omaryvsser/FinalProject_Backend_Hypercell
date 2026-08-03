package com.hypercell.event_ticketing_platform.Controller;

import com.hypercell.event_ticketing_platform.DTO.CreateSeatCategoryDto;
import com.hypercell.event_ticketing_platform.DTO.SeatCategoryResponseDto;
import com.hypercell.event_ticketing_platform.DTO.UpdateSeatCategoryDto;
import com.hypercell.event_ticketing_platform.Service.SeatCategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/events/{eventId}/seat-categories")
@RequiredArgsConstructor
public class SeatCategoryController {

    private final SeatCategoryService seatCategoryService;

    @PostMapping
    public ResponseEntity<SeatCategoryResponseDto> addSeatCategory(
            @PathVariable Long eventId,
            @Valid @RequestBody CreateSeatCategoryDto createSeatCategoryDto) {
        SeatCategoryResponseDto response = seatCategoryService.addSeatCategory(eventId, createSeatCategoryDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{categoryId}")
    public ResponseEntity<SeatCategoryResponseDto> updateSeatCategory(
            @PathVariable Long eventId,
            @PathVariable Long categoryId,
            @Valid @RequestBody UpdateSeatCategoryDto updateSeatCategoryDto) {
        SeatCategoryResponseDto response = seatCategoryService.updateSeatCategory(eventId, categoryId, updateSeatCategoryDto);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{categoryId}")
    public ResponseEntity<Void> deleteSeatCategory(
            @PathVariable Long eventId,
            @PathVariable Long categoryId) {
        seatCategoryService.deleteSeatCategory(eventId, categoryId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<SeatCategoryResponseDto>> getSeatCategoriesByEventId(@PathVariable Long eventId) {
        List<SeatCategoryResponseDto> response = seatCategoryService.getSeatCategoriesByEventId(eventId);
        return ResponseEntity.ok(response);
    }
}
