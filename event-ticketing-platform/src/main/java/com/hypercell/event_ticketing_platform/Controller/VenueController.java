package com.hypercell.event_ticketing_platform.Controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.hypercell.event_ticketing_platform.DTO.VenueDto;
import com.hypercell.event_ticketing_platform.Service.VenueService;
import jakarta.validation.Valid;

/**
 * REST Controller exposing Venue Management endpoints.
 * Public endpoints allow viewing venue catalogs, while mutation operations (create, update, delete)
 * are protected using Spring Security @PreAuthorize.
 */
@RestController
@RequestMapping("/api/venues")
public class VenueController {

    private final VenueService venueService;

    public VenueController(VenueService venueService) {
        this.venueService = venueService;
    }

    /**
     * POST /api/venues
     * Creates a new venue cinema location.
     * Security Guard: Accessible only by ADMIN or ORGANIZER roles.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<VenueDto.Response> createVenue(@Valid @RequestBody VenueDto.CreateRequest venueDto) {
        VenueDto.Response createdVenue = venueService.addVenue(venueDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdVenue);
    }

    /**
     * GET /api/venues
     * Retrieves all venue locations.
     * Public access permitted.
     */
    @GetMapping
    public ResponseEntity<List<VenueDto.Response>> getAllVenues() {
        List<VenueDto.Response> venues = venueService.getAllVenues();
        return ResponseEntity.ok(venues);
    }

    /**
     * GET /api/venues/{id}
     * Retrieves specific venue details by ID.
     * Public access permitted.
     */
    @GetMapping("/{id}")
    public ResponseEntity<VenueDto.Response> getVenueById(@PathVariable Long id) {
        VenueDto.Response venue = venueService.getVenueById(id);
        return ResponseEntity.ok(venue);
    }

    /**
     * PUT /api/venues/{id}
     * Updates an existing venue location.
     * Security Guard: Accessible only by ADMIN or ORGANIZER roles.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    public ResponseEntity<VenueDto.Response> updateVenue(@PathVariable Long id, @Valid @RequestBody VenueDto.UpdateRequest venueDto) {
        VenueDto.Response updatedVenue = venueService.updateVenue(id, venueDto);
        return ResponseEntity.ok(updatedVenue);
    }

    /**
     * DELETE /api/venues/{id}
     * Deletes a venue location.
     * Security Guard: Accessible only by ADMIN role.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> deleteVenue(@PathVariable Long id) {
        venueService.deleteVenue(id);
        return ResponseEntity.noContent().build();
    }
}
