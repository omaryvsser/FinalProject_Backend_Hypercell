package com.hypercell.event_ticketing_platform.Controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
// import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.hypercell.event_ticketing_platform.DTO.CreateVenueDto;
import com.hypercell.event_ticketing_platform.DTO.UpdateVenueDto;
import com.hypercell.event_ticketing_platform.DTO.VenueDto;
import com.hypercell.event_ticketing_platform.Service.VenueService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/venues")
public class VenueController {

    private final VenueService venueService;

    // Constructor Injection (Used instead of @Autowired for cleaner code)
    public VenueController(VenueService venueService) {
        this.venueService = venueService;
    }

    @PostMapping
    // @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<CreateVenueDto> createVenue(@Valid @RequestBody CreateVenueDto venueDto) {
        CreateVenueDto createdVenue = venueService.addVenue(venueDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdVenue);
    }

    @GetMapping
    public ResponseEntity<List<VenueDto>> getAllVenues() {
        List<VenueDto> venues = venueService.getAllVenues();
        return ResponseEntity.ok(venues);
    }

    @GetMapping("/{id}")
    public ResponseEntity<VenueDto> getVenueById(@PathVariable Long id) {
        VenueDto venue = venueService.getVenueById(id);
        return ResponseEntity.ok(venue);
    }

    @PutMapping("/{id}")
    // @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<VenueDto> updateVenue(@PathVariable Long id, @Valid @RequestBody UpdateVenueDto venueDto) {
        VenueDto updatedVenue = venueService.updateVenue(id, venueDto);
        return ResponseEntity.ok(updatedVenue);
    }

    @DeleteMapping("/{id}")
    // @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> deleteVenue(@PathVariable Long id) {
        venueService.deleteVenue(id);
        return ResponseEntity.noContent().build();
    }
}
